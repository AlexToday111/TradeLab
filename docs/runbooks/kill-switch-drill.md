# Kill Switch Drill

Target release: `v0.9.0-alpha.1`

## Preconditions

- A user can authenticate to the frontend.
- At least one live trading session exists.
- Real order submission remains disabled unless a separate production approval exists.

## Drill

1. Open `/live`.
2. Click `Kill switch`.
3. Verify `/live` shows kill switch `ACTIVE`.
4. Submit a guarded live order.
5. Confirm the order is rejected with `Kill switch is active`.
6. Open `/settings` and confirm the safety dashboard shows the active kill switch.
7. Click `Reset`.
8. Confirm `/live` and `/settings` return to clear kill switch state.

## Evidence

- Screenshot of `/live` safety state before and after reset.
- Risk audit row for `KILL_SWITCH_ACTIVATED`.
- Rejected order reason for the blocked order.
