import { ArrowUpRight, Github } from 'lucide-react'
import { motion } from 'motion/react'
import { useEffect, useState } from 'react'

const repositoryUrl =
  import.meta.env.VITE_REPOSITORY_URL ?? 'https://github.com/AlexToday111/TradeLab'
const applicationUrl =
  import.meta.env.VITE_APP_URL ?? 'http://localhost:3000/workspace'

const stats = [
  { value: `360${'\u00B0'}`, label: 'Research Loop' },
  { value: '24/7', label: 'Market Monitoring' },
  { value: '\u221E', label: 'Strategy Paths' },
]

export default function App() {
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })

  useEffect(() => {
    const handleMouseMove = (event: MouseEvent) => {
      setMousePosition({
        x: (event.clientX / window.innerWidth - 0.5) * 20,
        y: (event.clientY / window.innerHeight - 0.5) * 20,
      })
    }

    window.addEventListener('mousemove', handleMouseMove)
    return () => window.removeEventListener('mousemove', handleMouseMove)
  }, [])

  return (
    <div className="relative min-h-screen w-full overflow-hidden bg-[#0a0a0a] text-white">
      <div
        className="absolute inset-0 opacity-20"
        style={{
          backgroundImage: `
            linear-gradient(rgba(200, 242, 74, 0.1) 1px, transparent 1px),
            linear-gradient(90deg, rgba(200, 242, 74, 0.1) 1px, transparent 1px)
          `,
          backgroundSize: '50px 50px',
        }}
      />

      <div className="absolute inset-0 flex items-center justify-center overflow-hidden pointer-events-none">
        <motion.div
          className="absolute text-[20vw] font-bold opacity-[0.03] whitespace-nowrap"
          style={{
            color: 'transparent',
            fontFamily: 'Orbitron, sans-serif',
            WebkitTextStroke: '2px rgba(200, 242, 74, 0.3)',
            x: mousePosition.x * 0.5,
            y: mousePosition.y * 0.5,
          }}
        >
          T360 LAB
        </motion.div>
        <motion.div
          className="absolute top-[20%] text-[20vw] font-bold opacity-[0.02] whitespace-nowrap"
          style={{
            color: 'transparent',
            fontFamily: 'Orbitron, sans-serif',
            WebkitTextStroke: '2px rgba(200, 242, 74, 0.2)',
            x: mousePosition.x * -0.3,
            y: mousePosition.y * -0.3,
          }}
        >
          T360 LAB
        </motion.div>
      </div>

      <div className="absolute inset-0 pointer-events-none">
        <motion.div
          className="absolute top-1/4 left-1/4 h-64 w-64 rounded-full border border-[#c8f24a]/10"
          animate={{ rotate: 360 }}
          transition={{ duration: 40, ease: 'linear', repeat: Number.POSITIVE_INFINITY }}
        />
        <motion.div
          className="absolute right-1/4 bottom-1/4 h-96 w-96 rounded-full border border-[#c8f24a]/10"
          animate={{ rotate: -360 }}
          transition={{ duration: 50, ease: 'linear', repeat: Number.POSITIVE_INFINITY }}
        />
        <motion.div
          className="absolute top-1/2 left-1/2 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full border border-[#c8f24a]/5"
          animate={{ rotate: 360 }}
          transition={{ duration: 60, ease: 'linear', repeat: Number.POSITIVE_INFINITY }}
        />
      </div>

      <div className="absolute top-1/4 left-1/4 h-96 w-96 rounded-full bg-[#c8f24a] blur-[120px] opacity-10 pointer-events-none" />
      <div className="absolute right-1/4 bottom-1/4 h-96 w-96 rounded-full bg-[#c8f24a] blur-[120px] opacity-10 pointer-events-none" />

      <div className="absolute top-8 right-8 font-mono text-xs text-[#c8f24a]/40">landing</div>

      <div className="relative z-10 flex min-h-screen flex-col items-center justify-center px-6">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 1, ease: 'easeOut' }}
          className="relative mb-12"
        >
          <motion.div
            className="absolute inset-0 bg-[#c8f24a] opacity-30 blur-3xl"
            animate={{ opacity: [0.2, 0.4, 0.2] }}
            transition={{ duration: 3, ease: 'easeInOut', repeat: Number.POSITIVE_INFINITY }}
          />

          <div className="relative">
            <div
              className="absolute top-1/2 left-1/2 h-[180%] w-[140%] -translate-x-1/2 -translate-y-1/2 pointer-events-none md:h-[160%] md:w-[120%]"
              style={{ perspective: '1000px' }}
            >
              <motion.div
                className="absolute inset-0 rounded-full border-[0.5px] border-[#c8f24a]/20 shadow-[0_0_15px_rgba(200,242,74,0.15)]"
                initial={{ rotateX: 65, rotateY: 10, rotateZ: 0 }}
                animate={{ rotateZ: 360 }}
                transition={{ duration: 25, ease: 'linear', repeat: Number.POSITIVE_INFINITY }}
              >
                <div className="absolute top-0 left-1/2 h-1.5 w-1.5 -translate-x-1/2 -translate-y-1/2 rounded-full bg-[#c8f24a] shadow-[0_0_10px_#c8f24a]" />
              </motion.div>
            </div>

            <h1
              className="relative flex items-baseline justify-center text-center text-7xl font-black tracking-tight md:text-9xl"
              style={{ fontFamily: 'Orbitron, sans-serif' }}
            >
              <span className="text-white drop-shadow-[0_0_10px_rgba(255,255,255,0.1)]">
                T360{'\u00B0'}
              </span>
              <span className="ml-2 -translate-y-2 text-5xl font-bold text-[#c8f24a]/80 drop-shadow-[0_0_15px_rgba(200,242,74,0.2)] md:ml-4 md:-translate-y-4 md:text-7xl">
                LAB
              </span>
            </h1>

            <motion.div
              className="mt-6 h-px bg-gradient-to-r from-transparent via-[#c8f24a]/40 to-transparent"
              initial={{ width: 0 }}
              animate={{ width: '100%' }}
              transition={{ duration: 1.5, delay: 0.5 }}
            />
          </div>
        </motion.div>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1, delay: 0.3 }}
          className="mb-4 max-w-2xl text-center text-lg font-light uppercase tracking-[0.2em] text-gray-300 md:text-xl"
          style={{ fontFamily: 'Inter, sans-serif' }}
        >
          Build. Test. Explore.
        </motion.p>

        <motion.p
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1, delay: 0.45 }}
          className="mb-10 max-w-2xl text-center text-sm text-gray-400 md:text-base"
          style={{ fontFamily: 'Inter, sans-serif' }}
        >
          Strategy research, backtesting, and execution workflows in one product surface.
        </motion.p>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 1, delay: 0.7 }}
          className="flex flex-col gap-4 sm:flex-row"
        >
          <motion.a
            href={repositoryUrl}
            target="_blank"
            rel="noreferrer"
            className="group relative overflow-hidden rounded-lg bg-[#c8f24a] px-8 py-4 font-semibold text-black transition-all duration-300"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            style={{ fontFamily: 'Inter, sans-serif' }}
          >
            <div className="absolute inset-0 bg-[#c8f24a] opacity-0 blur-xl transition-opacity duration-300 group-hover:opacity-50" />
            <div className="relative flex items-center gap-2">
              <Github className="h-5 w-5" />
              <span>View Repository</span>
            </div>
          </motion.a>

          <motion.a
            href={applicationUrl}
            target="_blank"
            rel="noreferrer"
            className="group relative overflow-hidden rounded-lg border-2 border-[#c8f24a]/50 bg-transparent px-8 py-4 font-semibold text-[#c8f24a] transition-all duration-300 hover:border-[#c8f24a] hover:bg-[#c8f24a]/10"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            style={{ fontFamily: 'Inter, sans-serif' }}
          >
            <span className="relative flex items-center gap-2">
              Open Workspace
              <ArrowUpRight className="h-5 w-5" />
            </span>
          </motion.a>
        </motion.div>

        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1, delay: 1 }}
          className="mt-20 grid grid-cols-3 gap-8 md:gap-16"
        >
          {stats.map((stat) => (
            <div key={stat.label} className="text-center">
              <div
                className="text-3xl font-bold text-[#c8f24a] md:text-4xl"
                style={{ fontFamily: 'Orbitron, sans-serif' }}
              >
                {stat.value}
              </div>
              <div
                className="mt-1 text-xs text-gray-400 md:text-sm"
                style={{ fontFamily: 'Inter, sans-serif' }}
              >
                {stat.label}
              </div>
            </div>
          ))}
        </motion.div>
      </div>

      <div className="absolute bottom-8 left-1/2 -translate-x-1/2 whitespace-nowrap font-mono text-[10px] tracking-widest text-[#c8f24a]/40 md:text-xs">
        STRATEGY RESEARCH / BACKTESTING / OPTIMIZATION
      </div>

      <div className="absolute top-0 left-0 h-32 w-32 border-l-2 border-t-2 border-[#c8f24a]/20" />
      <div className="absolute top-0 right-0 h-32 w-32 border-r-2 border-t-2 border-[#c8f24a]/20" />
      <div className="absolute bottom-0 left-0 h-32 w-32 border-l-2 border-b-2 border-[#c8f24a]/20" />
      <div className="absolute right-0 bottom-0 h-32 w-32 border-r-2 border-b-2 border-[#c8f24a]/20" />
    </div>
  )
}
