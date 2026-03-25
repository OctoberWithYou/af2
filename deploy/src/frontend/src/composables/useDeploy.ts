import { ref } from 'vue'
import { api } from '@/api'
import type { DeployConfig, DeployStats } from '@/types'

export function useDeploy() {
  const configs = ref<DeployConfig[]>([])
  const stats = ref<DeployStats | null>(null)
  const loading = ref(false)

  const loadConfigs = async () => {
    loading.value = true
    try {
      const result = await api.getConfigs()
      if (result.success && result.data) {
        configs.value = result.data
      }
    } finally {
      loading.value = false
    }
  }

  const loadStats = async () => {
    try {
      const result = await api.getStats()
      if (result.success && result.data) {
        stats.value = result.data
      }
    } catch {
      // 忽略错误
    }
  }

  const createConfig = async (name: string, type: 'AGENT' | 'SERVER', configJson: string) => {
    const result = await api.createConfig({ name, type, configJson: JSON.parse(configJson || '{}') })
    if (result.success) {
      await loadConfigs()
      await loadStats()
    }
    return result
  }

  const updateConfig = async (id: number, config: Partial<DeployConfig>) => {
    const result = await api.updateConfig(id, config)
    if (result.success) {
      await loadConfigs()
    }
    return result
  }

  const deleteConfig = async (id: number) => {
    const result = await api.deleteConfig(id)
    if (result.success) {
      await loadConfigs()
      await loadStats()
    }
    return result
  }

  const deployConfig = async (id: number) => {
    const result = await api.deploy(id)
    if (result.success) {
      await loadConfigs()
      await loadStats()
    }
    return result
  }

  const stopConfig = async (id: number) => {
    const result = await api.stop(id)
    if (result.success) {
      await loadConfigs()
      await loadStats()
    }
    return result
  }

  return {
    configs,
    stats,
    loading,
    loadConfigs,
    loadStats,
    createConfig,
    updateConfig,
    deleteConfig,
    deployConfig,
    stopConfig,
  }
}
