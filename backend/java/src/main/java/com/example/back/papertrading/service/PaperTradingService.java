package com.example.back.papertrading.service;

import com.example.back.auth.security.AuthContext;
import com.example.back.backtest.exception.BacktestResourceNotFoundException;
import com.example.back.papertrading.dto.CreatePaperOrderRequest;
import com.example.back.papertrading.dto.CreatePaperSessionRequest;
import com.example.back.papertrading.dto.PaperFillResponse;
import com.example.back.papertrading.dto.PaperOrderResponse;
import com.example.back.papertrading.dto.PaperPositionResponse;
import com.example.back.papertrading.dto.PaperSessionResponse;
import com.example.back.papertrading.dto.PaperSummaryResponse;
import com.example.back.papertrading.entity.PaperFillEntity;
import com.example.back.papertrading.entity.PaperOrderEntity;
import com.example.back.papertrading.entity.PaperOrderSide;
import com.example.back.papertrading.entity.PaperOrderStatus;
import com.example.back.papertrading.entity.PaperOrderType;
import com.example.back.papertrading.entity.PaperPositionEntity;
import com.example.back.papertrading.entity.PaperSessionStatus;
import com.example.back.papertrading.entity.PaperTradingSessionEntity;
import com.example.back.papertrading.repository.PaperFillRepository;
import com.example.back.papertrading.repository.PaperOrderRepository;
import com.example.back.papertrading.repository.PaperPositionRepository;
import com.example.back.papertrading.repository.PaperTradingSessionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaperTradingService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.001");
    private static final BigDecimal MAX_ORDER_NOTIONAL = new BigDecimal("100000.00000000");
    private static final int MONEY_SCALE = 8;

    private final PaperTradingSessionRepository sessionRepository;
    private final PaperOrderRepository orderRepository;
    private final PaperFillRepository fillRepository;
    private final PaperPositionRepository positionRepository;
    private final ExchangeAdapter exchangeAdapter;
    private final PaperTradingMapper mapper;

    @Transactional
    public PaperSessionResponse createSession(CreatePaperSessionRequest request) {
        Long userId = AuthContext.requireUserId();
        PaperTradingSessionEntity session = new PaperTradingSessionEntity();
        session.setUserId(userId);
        session.setName(request.name().trim());
        session.setExchange(request.exchange().trim().toLowerCase());
        session.setSymbol(request.symbol().trim().toUpperCase());
        session.setTimeframe(request.timeframe().trim());
        session.setStatus(PaperSessionStatus.CREATED);
        session.setInitialBalance(scale(request.initialBalance()));
        session.setCurrentBalance(scale(request.initialBalance()));
        session.setBaseCurrency(request.baseCurrency().trim().toUpperCase());
        session.setQuoteCurrency(request.quoteCurrency().trim().toUpperCase());

        PaperTradingSessionEntity saved = sessionRepository.save(session);
        log.info(
                "Paper trading session created user_id={} session_id={} symbol={}",
                userId,
                saved.getId(),
                saved.getSymbol()
        );
        return mapper.toSessionResponse(saved);
    }

    public List<PaperSessionResponse> listSessions() {
        Long userId = AuthContext.requireUserId();
        return sessionRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toSessionResponse)
                .toList();
    }

    public PaperSessionResponse getSession(Long sessionId) {
        return mapper.toSessionResponse(requireOwnedSession(sessionId));
    }

    @Transactional
    public PaperSessionResponse startSession(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        if (session.getStatus() == PaperSessionStatus.STOPPED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stopped sessions cannot be restarted");
        }
        session.setStatus(PaperSessionStatus.RUNNING);
        if (session.getStartedAt() == null) {
            session.setStartedAt(Instant.now());
        }
        session.setStoppedAt(null);
        PaperTradingSessionEntity saved = sessionRepository.save(session);
        log.info("Paper session started user_id={} session_id={}", saved.getUserId(), saved.getId());
        return mapper.toSessionResponse(saved);
    }

    @Transactional
    public PaperSessionResponse pauseSession(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        if (session.getStatus() != PaperSessionStatus.RUNNING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only running sessions can be paused");
        }
        session.setStatus(PaperSessionStatus.PAUSED);
        PaperTradingSessionEntity saved = sessionRepository.save(session);
        log.info("Paper session paused user_id={} session_id={}", saved.getUserId(), saved.getId());
        return mapper.toSessionResponse(saved);
    }

    @Transactional
    public PaperSessionResponse stopSession(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        if (session.getStatus() != PaperSessionStatus.STOPPED) {
            session.setStatus(PaperSessionStatus.STOPPED);
            session.setStoppedAt(Instant.now());
        }
        PaperTradingSessionEntity saved = sessionRepository.save(session);
        log.info("Paper session stopped user_id={} session_id={}", saved.getUserId(), saved.getId());
        return mapper.toSessionResponse(saved);
    }

    @Transactional
    public PaperOrderResponse placeOrder(Long sessionId, CreatePaperOrderRequest request) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        PaperOrderEntity order = buildOrder(session, request);
        BigDecimal riskPrice = resolveRiskPrice(session, order);
        String rejectionReason = validateRisk(session, order, riskPrice);
        if (rejectionReason != null) {
            return rejectOrder(order, rejectionReason);
        }

        exchangeAdapter.placeOrder(session, order);
        BigDecimal latestPrice = exchangeAdapter.getLatestPrice(session).orElse(null);
        if (shouldFill(order, latestPrice)) {
            fillOrder(session, order, latestPrice);
        } else {
            order.setStatus(PaperOrderStatus.ACCEPTED);
            orderRepository.save(order);
            log.info(
                    "Paper order accepted user_id={} session_id={} order_id={} symbol={} side={} status={}",
                    order.getUserId(),
                    order.getSessionId(),
                    order.getId(),
                    order.getSymbol(),
                    order.getSide(),
                    order.getStatus()
            );
        }
        return mapper.toOrderResponse(order);
    }

    public List<PaperOrderResponse> listOrders(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        return orderRepository
                .findAllBySessionIdAndUserIdOrderByCreatedAtDesc(session.getId(), session.getUserId())
                .stream()
                .map(mapper::toOrderResponse)
                .toList();
    }

    public PaperOrderResponse getOrder(Long orderId) {
        Long userId = AuthContext.requireUserId();
        PaperOrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Paper order not found: " + orderId));
        return mapper.toOrderResponse(exchangeAdapter.getOrder(order));
    }

    @Transactional
    public PaperOrderResponse cancelOrder(Long orderId) {
        Long userId = AuthContext.requireUserId();
        PaperOrderEntity order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException("Paper order not found: " + orderId));
        if (order.getStatus() != PaperOrderStatus.ACCEPTED && order.getStatus() != PaperOrderStatus.NEW) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only new or accepted paper orders can be canceled");
        }
        exchangeAdapter.cancelOrder(order);
        order.setStatus(PaperOrderStatus.CANCELED);
        PaperOrderEntity saved = orderRepository.save(order);
        log.info(
                "Paper order canceled user_id={} session_id={} order_id={} symbol={}",
                saved.getUserId(),
                saved.getSessionId(),
                saved.getId(),
                saved.getSymbol()
        );
        return mapper.toOrderResponse(saved);
    }

    public List<PaperPositionResponse> listPositions(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        return exchangeAdapter.getPositions(session).stream()
                .map(mapper::toPositionResponse)
                .toList();
    }

    public List<PaperFillResponse> listFills(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        return fillRepository.findAllBySessionIdOrderByExecutedAtDesc(session.getId()).stream()
                .map(mapper::toFillResponse)
                .toList();
    }

    public PaperSummaryResponse getSummary(Long sessionId) {
        PaperTradingSessionEntity session = requireOwnedSession(sessionId);
        List<PaperPositionEntity> positions = exchangeAdapter.getPositions(session);
        BigDecimal unrealized = positions.stream()
                .map(PaperPositionEntity::getUnrealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realized = positions.stream()
                .map(PaperPositionEntity::getRealizedPnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal equity = scale(session.getCurrentBalance().add(positionMarketValue(positions)).add(unrealized));
        return new PaperSummaryResponse(
                session.getId(),
                session.getStatus(),
                session.getInitialBalance(),
                session.getCurrentBalance(),
                scale(realized),
                scale(unrealized),
                equity,
                positions.size(),
                orderRepository.findAllBySessionIdAndUserIdOrderByCreatedAtDesc(
                        session.getId(),
                        session.getUserId()
                ).size(),
                fillRepository.findAllBySessionIdOrderByExecutedAtDesc(session.getId()).size()
        );
    }

    private PaperTradingSessionEntity requireOwnedSession(Long sessionId) {
        Long userId = AuthContext.requireUserId();
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BacktestResourceNotFoundException(
                        "Paper trading session not found: " + sessionId
                ));
    }

    private PaperOrderEntity buildOrder(PaperTradingSessionEntity session, CreatePaperOrderRequest request) {
        PaperOrderEntity order = new PaperOrderEntity();
        order.setSessionId(session.getId());
        order.setUserId(session.getUserId());
        order.setSymbol(session.getSymbol());
        order.setSide(request.side());
        order.setType(request.type());
        order.setStatus(PaperOrderStatus.NEW);
        order.setQuantity(scale(request.quantity()));
        order.setPrice(request.price() == null ? null : scale(request.price()));
        order.setFilledQuantity(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return orderRepository.save(order);
    }

    private BigDecimal resolveRiskPrice(PaperTradingSessionEntity session, PaperOrderEntity order) {
        if (order.getType() == PaperOrderType.LIMIT) {
            return order.getPrice();
        }
        return exchangeAdapter.getLatestPrice(session).orElse(null);
    }

    private String validateRisk(
            PaperTradingSessionEntity session,
            PaperOrderEntity order,
            BigDecimal riskPrice
    ) {
        if (session.getStatus() != PaperSessionStatus.RUNNING) {
            return "Session must be RUNNING";
        }
        if (order.getQuantity() == null || order.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            return "Quantity must be positive";
        }
        if (!session.getSymbol().equals(order.getSymbol())) {
            return "Order symbol must match session symbol";
        }
        if (order.getType() == PaperOrderType.LIMIT
                && (order.getPrice() == null || order.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            return "Limit price must be positive";
        }
        if (riskPrice == null || riskPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return "Latest market price is unavailable";
        }

        BigDecimal notional = notional(order.getQuantity(), riskPrice);
        if (notional.compareTo(MAX_ORDER_NOTIONAL) > 0) {
            return "Order notional exceeds max limit " + MAX_ORDER_NOTIONAL;
        }
        if (order.getSide() == PaperOrderSide.BUY) {
            BigDecimal requiredBalance = notional.add(fee(notional));
            if (session.getCurrentBalance().compareTo(requiredBalance) < 0) {
                return "Insufficient balance";
            }
        } else {
            BigDecimal positionQuantity = positionRepository
                    .findBySessionIdAndSymbol(session.getId(), order.getSymbol())
                    .map(PaperPositionEntity::getQuantity)
                    .orElse(BigDecimal.ZERO);
            if (positionQuantity.compareTo(order.getQuantity()) < 0) {
                return "Insufficient position";
            }
        }
        return null;
    }

    private PaperOrderResponse rejectOrder(PaperOrderEntity order, String reason) {
        order.setStatus(PaperOrderStatus.REJECTED);
        order.setRejectedReason(reason);
        PaperOrderEntity saved = orderRepository.save(order);
        log.info(
                "Paper order rejected user_id={} session_id={} order_id={} symbol={} side={} reason={}",
                saved.getUserId(),
                saved.getSessionId(),
                saved.getId(),
                saved.getSymbol(),
                saved.getSide(),
                reason
        );
        return mapper.toOrderResponse(saved);
    }

    private boolean shouldFill(PaperOrderEntity order, BigDecimal latestPrice) {
        if (latestPrice == null) {
            return false;
        }
        if (order.getType() == PaperOrderType.MARKET) {
            return true;
        }
        if (order.getSide() == PaperOrderSide.BUY) {
            return latestPrice.compareTo(order.getPrice()) <= 0;
        }
        return latestPrice.compareTo(order.getPrice()) >= 0;
    }

    private void fillOrder(
            PaperTradingSessionEntity session,
            PaperOrderEntity order,
            BigDecimal fillPrice
    ) {
        BigDecimal fillNotional = notional(order.getQuantity(), fillPrice);
        BigDecimal fillFee = fee(fillNotional);
        order.setStatus(PaperOrderStatus.FILLED);
        order.setFilledQuantity(order.getQuantity());
        order.setAverageFillPrice(scale(fillPrice));
        order.setFilledAt(Instant.now());
        orderRepository.save(order);

        PaperFillEntity fill = new PaperFillEntity();
        fill.setOrderId(order.getId());
        fill.setSessionId(session.getId());
        fill.setSymbol(order.getSymbol());
        fill.setSide(order.getSide());
        fill.setQuantity(order.getQuantity());
        fill.setPrice(scale(fillPrice));
        fill.setFee(fillFee);
        fill.setFeeCurrency(session.getQuoteCurrency());
        fill.setExecutedAt(order.getFilledAt());
        fillRepository.save(fill);

        updateBalance(session, order, fillNotional, fillFee);
        updatePosition(session, order, fillPrice, fillFee);
        sessionRepository.save(session);
        log.info(
                "Paper order filled user_id={} session_id={} order_id={} symbol={} side={} quantity={} price={} fee={}",
                order.getUserId(),
                order.getSessionId(),
                order.getId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                fillPrice,
                fillFee
        );
    }

    private void updateBalance(
            PaperTradingSessionEntity session,
            PaperOrderEntity order,
            BigDecimal fillNotional,
            BigDecimal fillFee
    ) {
        if (order.getSide() == PaperOrderSide.BUY) {
            session.setCurrentBalance(scale(session.getCurrentBalance().subtract(fillNotional).subtract(fillFee)));
        } else {
            session.setCurrentBalance(scale(session.getCurrentBalance().add(fillNotional).subtract(fillFee)));
        }
    }

    private void updatePosition(
            PaperTradingSessionEntity session,
            PaperOrderEntity order,
            BigDecimal fillPrice,
            BigDecimal fillFee
    ) {
        PaperPositionEntity position = positionRepository
                .findBySessionIdAndSymbol(session.getId(), order.getSymbol())
                .orElseGet(() -> newPosition(session));
        if (order.getSide() == PaperOrderSide.BUY) {
            applyBuy(position, order.getQuantity(), fillPrice);
        } else {
            applySell(position, order.getQuantity(), fillPrice, fillFee);
        }
        position.setUnrealizedPnl(unrealizedPnl(position, fillPrice));
        positionRepository.save(position);
    }

    private PaperPositionEntity newPosition(PaperTradingSessionEntity session) {
        PaperPositionEntity position = new PaperPositionEntity();
        position.setSessionId(session.getId());
        position.setSymbol(session.getSymbol());
        position.setQuantity(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        position.setAverageEntryPrice(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        position.setRealizedPnl(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        position.setUnrealizedPnl(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        return position;
    }

    private void applyBuy(PaperPositionEntity position, BigDecimal quantity, BigDecimal price) {
        BigDecimal currentQuantity = position.getQuantity();
        BigDecimal newQuantity = currentQuantity.add(quantity);
        BigDecimal currentCost = currentQuantity.multiply(position.getAverageEntryPrice());
        BigDecimal addedCost = quantity.multiply(price);
        position.setQuantity(scale(newQuantity));
        position.setAverageEntryPrice(scale(currentCost.add(addedCost).divide(newQuantity, MONEY_SCALE, RoundingMode.HALF_UP)));
    }

    private void applySell(
            PaperPositionEntity position,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal fillFee
    ) {
        BigDecimal realized = price.subtract(position.getAverageEntryPrice()).multiply(quantity).subtract(fillFee);
        BigDecimal newQuantity = position.getQuantity().subtract(quantity);
        position.setQuantity(scale(newQuantity));
        position.setRealizedPnl(scale(position.getRealizedPnl().add(realized)));
        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            position.setAverageEntryPrice(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP));
        }
    }

    private BigDecimal positionMarketValue(List<PaperPositionEntity> positions) {
        return positions.stream()
                .map(position -> position.getQuantity().multiply(position.getAverageEntryPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal unrealizedPnl(PaperPositionEntity position, BigDecimal markPrice) {
        return scale(markPrice.subtract(position.getAverageEntryPrice()).multiply(position.getQuantity()));
    }

    private BigDecimal notional(BigDecimal quantity, BigDecimal price) {
        return scale(quantity.multiply(price));
    }

    private BigDecimal fee(BigDecimal notional) {
        return scale(notional.multiply(FEE_RATE));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
