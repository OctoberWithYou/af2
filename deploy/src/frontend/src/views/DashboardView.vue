<template>
  <div class="dashboard">
    <!-- 头部 -->
    <el-card class="header-card">
      <div class="header">
        <h1>AI Forward 部署管理平台</h1>
        <div class="user-info">
          <span>{{ currentUser }}</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value blue">{{ stats?.totalAgents ?? 0 }}</div>
          <div class="stat-label">Agent 总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value green">{{ stats?.runningAgents ?? 0 }}</div>
          <div class="stat-label">运行中 Agent</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value blue">{{ stats?.totalServers ?? 0 }}</div>
          <div class="stat-label">Server 总数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value green">{{ stats?.runningServers ?? 0 }}</div>
          <div class="stat-label">运行中 Server</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置列表 -->
    <el-card class="content-card">
      <template #header>
        <div class="toolbar">
          <h2>部署配置</h2>
          <el-button type="primary" @click="showAddDialog">+ 新建配置</el-button>
        </div>
      </template>

      <el-table :data="configs" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'AGENT' ? 'success' : 'primary'">
              {{ row.type === 'AGENT' ? 'Agent' : 'Server' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配置" width="100">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewConfig(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              type="success"
              size="small"
              :disabled="row.status === 'RUNNING'"
              @click="handleDeploy(row.id)"
            >
              部署
            </el-button>
            <el-button
              type="warning"
              size="small"
              :disabled="row.status !== 'RUNNING'"
              @click="handleStop(row.id)"
            >
              停止
            </el-button>
            <el-button type="info" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="configs.length === 0 && !loading" description="暂无配置" />
    </el-card>

    <!-- 配置对话框 -->
    <ConfigDialog
      v-model:visible="dialogVisible"
      ref="configDialogRef"
      :editing-id="editingId"
      :existing-config="currentEditConfig"
      @save="saveConfig"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuth } from '@/composables/useAuth'
import { useDeploy } from '@/composables/useDeploy'
import type { DeployConfig } from '@/types'
import ConfigDialog from '@/components/ConfigDialog.vue'

const router = useRouter()
const { currentUser, logout } = useAuth()
const { configs, stats, loading, loadConfigs, loadStats, createConfig, updateConfig, deleteConfig, deployConfig, stopConfig } = useDeploy()

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const currentEditConfig = ref<DeployConfig | null>(null)
const configDialogRef = ref<InstanceType<typeof ConfigDialog>>()

const getStatusType = (status: string) => {
  switch (status) {
    case 'RUNNING':
      return 'success'
    case 'DEPLOYING':
      return 'warning'
    default:
      return 'info'
  }
}

const viewConfig = (config: DeployConfig) => {
  ElMessageBox.alert(JSON.stringify(config.configJson, null, 2), '配置详情', {
    confirmButtonText: '关闭',
  })
}

const showAddDialog = () => {
  editingId.value = null
  currentEditConfig.value = null
  configDialogRef.value?.resetForm()
  dialogVisible.value = true
}

const handleEdit = (config: DeployConfig) => {
  editingId.value = config.id
  currentEditConfig.value = { ...config }
  dialogVisible.value = true
}

const saveConfig = async (configData: { name: string; type: 'AGENT' | 'SERVER'; configJson: Record<string, unknown> }) => {
  if (!configData.name) {
    ElMessage.error('请输入名称')
    return
  }

  const result = editingId.value
    ? await updateConfig(editingId.value, {
        id: editingId.value,
        name: configData.name,
        type: configData.type,
        configJson: configData.configJson,
        status: 'STOPPED',
        configJsonStr: '',
        createdAt: '',
        updatedAt: '',
        createdBy: '',
      })
    : await createConfig(configData.name, configData.type, JSON.stringify(configData.configJson))

  if (result.success) {
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadConfigs()
    loadStats()
  } else {
    ElMessage.error(result.message || '保存失败')
  }
}

const handleDeploy = async (id: number) => {
  const result = await deployConfig(id)
  if (result.success) {
    ElMessage.success('部署已启动')
    setTimeout(() => {
      loadConfigs()
      loadStats()
    }, 1000)
  } else {
    ElMessage.error(result.message || '部署失败')
  }
}

const handleStop = async (id: number) => {
  const result = await stopConfig(id)
  if (result.success) {
    ElMessage.success('停止成功')
    setTimeout(() => {
      loadConfigs()
      loadStats()
    }, 1000)
  } else {
    ElMessage.error(result.message || '停止失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    const result = await deleteConfig(id)
    if (result.success) {
      ElMessage.success('删除成功')
      loadConfigs()
      loadStats()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

const handleLogout = () => {
  logout()
}

onMounted(() => {
  loadConfigs()
  loadStats()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.header-card {
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  margin: 0;
  color: #333;
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
}

.stat-value.blue {
  color: #667eea;
}

.stat-value.green {
  color: #67c23a;
}

.stat-label {
  color: #666;
  margin-top: 5px;
}

.content-card {
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar h2 {
  margin: 0;
  font-size: 18px;
}
</style>
