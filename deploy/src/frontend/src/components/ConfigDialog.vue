<template>
  <el-dialog
    v-model="dialogVisible"
    :title="editingId ? '编辑配置' : '新建配置'"
    width="700px"
    :close-on-click-modal="false"
  >
    <el-form :model="configForm" label-position="top" label-width="140px">
      <!-- 基础配置 -->
      <el-form-item label="配置名称" required>
        <el-input v-model="configForm.name" placeholder="请输入配置名称，如：prod-agent-01" />
      </el-form-item>

      <el-form-item label="配置类型" required>
        <el-radio-group v-model="configForm.type">
          <el-radio-button value="AGENT">Agent (内网代理)</el-radio-button>
          <el-radio-button value="SERVER">Server (公网转发)</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <!-- Agent 配置表单 -->
      <template v-if="configForm.type === 'AGENT'">
        <el-divider content-position="left">Agent 配置</el-divider>

        <el-form-item label="Agent ID">
          <el-input
            v-model="agentConfig.agentId"
            placeholder="留空自动生成"
            clearable
          />
          <div class="form-tip">Agent 的唯一标识符，留空则启动时自动生成</div>
        </el-form-item>

        <el-form-item label="Agent 名称">
          <el-input v-model="agentConfig.name" placeholder="AI Forward Agent" />
          <div class="form-tip">Agent 的显示名称，用于在管理界面识别</div>
        </el-form-item>

        <el-form-item label="描述信息">
          <el-input
            v-model="agentConfig.description"
            type="textarea"
            :rows="2"
            placeholder="内网 AI 模型转发代理"
          />
          <div class="form-tip">Agent 的详细描述信息</div>
        </el-form-item>

        <el-form-item label="Server 地址" required>
          <el-input
            v-model="agentConfig.serverUrl"
            placeholder="ws://localhost:8888/ws"
          />
          <div class="form-tip">
            公网 Server 的 WebSocket 地址。<br/>
            示例：<code>ws://203.0.113.10:8888/ws</code> (未启用 SSL)<br/>
            示例：<code>wss://203.0.113.10:8888/ws</code> (启用 SSL)
          </div>
        </el-form-item>

        <el-form-item label="SSL/TLS 启用">
          <el-switch v-model="agentConfig.sslEnabled" />
          <div class="form-tip">启用后使用 wss:// 加密连接，需要 Server 配置 SSL 证书</div>
        </el-form-item>

        <el-collapse v-model="agentAdvancedCollapse" class="advanced-config">
          <el-collapse-item title="高级配置" name="advanced">
            <el-form-item label="连接超时 (ms)">
              <el-input-number
                v-model="agentConfig.connectTimeout"
                :min="1000"
                :max="60000"
                :step="1000"
                style="width: 100%"
              />
              <div class="form-tip">连接 Server 的超时时间，范围：1000-60000ms，默认：5000ms</div>
            </el-form-item>

            <el-form-item label="心跳间隔 (秒)">
              <el-input-number
                v-model="agentConfig.heartbeatInterval"
                :min="5"
                :max="300"
                :step="5"
                style="width: 100%"
              />
              <div class="form-tip">发送心跳消息的间隔，范围：5-300 秒，默认：30 秒</div>
            </el-form-item>

            <el-form-item label="重连间隔 (秒)">
              <el-input-number
                v-model="agentConfig.reconnectInterval"
                :min="1"
                :max="60"
                :step="1"
                style="width: 100%"
              />
              <div class="form-tip">连接断开后重试间隔，范围：1-60 秒，默认：10 秒</div>
            </el-form-item>

            <el-form-item label="最大重试次数">
              <el-input-number
                v-model="agentConfig.maxRetries"
                :min="1"
                :max="20"
                :step="1"
                style="width: 100%"
              />
              <div class="form-tip">连接失败时的最大重试次数，范围：1-20 次，默认：5 次</div>
            </el-form-item>

            <el-form-item label="允许的目标 URL">
              <el-select
                v-model="agentConfig.allowedTargets"
                multiple
                allow-create
                filterable
                placeholder="输入允许转发的目标 URL，回车添加"
                style="width: 100%"
              >
                <el-option label="允许所有 (*)" value="*" />
              </el-select>
              <div class="form-tip">
                Agent 允许转发的目标 AI 模型地址列表。<br/>
                示例：<code>http://localhost:11434</code> (Ollama)<br/>
                示例：<code>http://localhost:5000</code> (LocalAI)<br/>
                留空或选择 (*) 表示允许所有目标
              </div>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </template>

      <!-- Server 配置表单 -->
      <template v-if="configForm.type === 'SERVER'">
        <el-divider content-position="left">Server 配置</el-divider>

        <el-form-item label="HTTP 端口">
          <el-input-number
            v-model="serverConfig.port"
            :min="1024"
            :max="65535"
            :step="1"
            style="width: 100%"
          />
          <div class="form-tip">HTTP REST API 服务端口，范围：1024-65535，默认：8080</div>
        </el-form-item>

        <el-form-item label="WebSocket 端口">
          <el-input-number
            v-model="serverConfig.wsPort"
            :min="1024"
            :max="65535"
            :step="1"
            style="width: 100%"
          />
          <div class="form-tip">Agent 连接的 WebSocket 端口，范围：1024-65535，默认：8888</div>
        </el-form-item>

        <el-form-item label="SSL/TLS 启用">
          <el-switch v-model="serverConfig.sslEnabled" />
          <div class="form-tip">启用后使用 https:// 和 wss:// 加密连接，需要配置证书</div>
        </el-form-item>

        <template v-if="serverConfig.sslEnabled">
          <el-form-item label="密钥库路径" required>
            <el-input
              v-model="serverConfig.keyStorePath"
              placeholder="classpath:keystore.jks 或 /path/to/keystore.jks"
            />
            <div class="form-tip">
              SSL 密钥库文件路径。<br/>
              示例：<code>classpath:keystore.jks</code> (类路径下)<br/>
              示例：<code>/etc/ssl/keystore.jks</code> (绝对路径)
            </div>
          </el-form-item>

          <el-form-item label="密钥库密码" required>
            <el-input
              v-model="serverConfig.keyStorePassword"
              type="password"
              show-password
              placeholder="密钥库密码"
            />
            <div class="form-tip">SSL 密钥库的访问密码</div>
          </el-form-item>
        </template>

        <el-collapse v-model="serverAdvancedCollapse" class="advanced-config">
          <el-collapse-item title="高级配置" name="advanced">
            <el-form-item label="心跳检测间隔 (秒)">
              <el-input-number
                v-model="serverConfig.heartbeatInterval"
                :min="5"
                :max="300"
                :step="5"
                style="width: 100%"
              />
              <div class="form-tip">检测 Agent 心跳的间隔，范围：5-300 秒，默认：30 秒</div>
            </el-form-item>

            <el-form-item label="最大空闲时间 (秒)">
              <el-input-number
                v-model="serverConfig.maxIdleTime"
                :min="30"
                :max="600"
                :step="30"
                style="width: 100%"
              />
              <div class="form-tip">Agent 空闲超时时间，超时后断开连接，范围：30-600 秒，默认：120 秒</div>
            </el-form-item>

            <el-form-item label="认证用户名">
              <el-input v-model="serverConfig.authUsername" placeholder="admin" />
              <div class="form-tip">Basic Auth 认证的用户名，默认：admin</div>
            </el-form-item>

            <el-form-item label="认证密码">
              <el-input
                v-model="serverConfig.authPassword"
                type="password"
                show-password
                placeholder="Sys_ljc_123"
              />
              <div class="form-tip">Basic Auth 认证的密码，默认：Sys_ljc_123</div>
            </el-form-item>
          </el-collapse-item>
        </el-collapse>
      </template>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { AgentConfig, ServerConfig } from '@/types'

interface ConfigForm {
  name: string
  type: 'AGENT' | 'SERVER'
  configJsonStr: string
}

interface Props {
  editingId: number | null
  existingConfig: {
    id: number
    name: string
    type: 'AGENT' | 'SERVER'
    configJson: Record<string, unknown>
  } | null
}

const props = withDefaults(defineProps<Props>(), {
  editingId: null,
  existingConfig: null,
})

const dialogVisible = defineModel<boolean>('visible', { required: true })
const emit = defineEmits<{
  save: [config: { name: string; type: 'AGENT' | 'SERVER'; configJson: Record<string, unknown> }]
}>()

const agentAdvancedCollapse = ref<string[]>([])
const serverAdvancedCollapse = ref<string[]>([])

const agentConfig = reactive<AgentConfig>({
  agentId: '',
  name: 'AI Forward Agent',
  description: '内网 AI 模型转发代理',
  serverUrl: 'ws://localhost:8888/ws',
  sslEnabled: false,
  connectTimeout: 5000,
  heartbeatInterval: 30,
  reconnectInterval: 10,
  maxRetries: 5,
  allowedTargets: ['*'],
})

const serverConfig = reactive<ServerConfig>({
  port: 8080,
  wsPort: 8888,
  sslEnabled: false,
  keyStorePath: '',
  keyStorePassword: '',
  heartbeatInterval: 30,
  maxIdleTime: 120,
  authUsername: 'admin',
  authPassword: 'Sys_ljc_123',
})

const configForm = reactive<ConfigForm>({
  name: '',
  type: 'AGENT',
  configJsonStr: '',
})

// 监听类型切换，重置高级配置折叠状态
watch(() => configForm.type, () => {
  agentAdvancedCollapse.value = []
  serverAdvancedCollapse.value = []
})

// 监听编辑配置变化，填充表单
watch(() => props.existingConfig, (newConfig) => {
  if (newConfig) {
    configForm.name = newConfig.name
    configForm.type = newConfig.type

    const json = newConfig.configJson as any
    if (newConfig.type === 'AGENT') {
      Object.assign(agentConfig, {
        agentId: json.agentId || '',
        name: json.name || 'AI Forward Agent',
        description: json.description || '内网 AI 模型转发代理',
        serverUrl: json.serverUrl || 'ws://localhost:8888/ws',
        sslEnabled: json.sslEnabled || false,
        connectTimeout: json.connectTimeout || 5000,
        heartbeatInterval: json.heartbeatInterval || 30,
        reconnectInterval: json.reconnectInterval || 10,
        maxRetries: json.maxRetries || 5,
        allowedTargets: json.allowedTargets || ['*'],
      })
    } else {
      Object.assign(serverConfig, {
        port: json.port || 8080,
        wsPort: json.wsPort || 8888,
        sslEnabled: json.sslEnabled || false,
        keyStorePath: json.keyStorePath || '',
        keyStorePassword: json.keyStorePassword || '',
        heartbeatInterval: json.heartbeatInterval || 30,
        maxIdleTime: json.maxIdleTime || 120,
        authUsername: json.authUsername || 'admin',
        authPassword: json.authPassword || 'Sys_ljc_123',
      })
    }
  }
}, { immediate: true })

const getConfigJson = (): Record<string, unknown> => {
  if (configForm.type === 'AGENT') {
    return { ...agentConfig } as Record<string, unknown>
  } else {
    return { ...serverConfig } as Record<string, unknown>
  }
}

const handleSave = () => {
  emit('save', {
    name: configForm.name,
    type: configForm.type,
    configJson: getConfigJson(),
  })
}

const resetForm = () => {
  configForm.name = ''
  configForm.type = 'AGENT'
  configForm.configJsonStr = ''

  Object.assign(agentConfig, {
    agentId: '',
    name: 'AI Forward Agent',
    description: '内网 AI 模型转发代理',
    serverUrl: 'ws://localhost:8888/ws',
    sslEnabled: false,
    connectTimeout: 5000,
    heartbeatInterval: 30,
    reconnectInterval: 10,
    maxRetries: 5,
    allowedTargets: ['*'],
  })

  Object.assign(serverConfig, {
    port: 8080,
    wsPort: 8888,
    sslEnabled: false,
    keyStorePath: '',
    keyStorePassword: '',
    heartbeatInterval: 30,
    maxIdleTime: 120,
    authUsername: 'admin',
    authPassword: 'Sys_ljc_123',
  })

  agentAdvancedCollapse.value = []
  serverAdvancedCollapse.value = []
}

defineExpose({
  resetForm,
  getConfigJson,
  configForm,
  agentConfig,
  serverConfig,
})
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 4px;
}

.form-tip code {
  background-color: #f4f4f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', Courier, monospace;
  color: #333;
}

.advanced-config {
  margin-top: 16px;
}

.advanced-config :deep(.el-collapse-item__header) {
  background-color: #f5f7fa;
  padding-left: 16px;
}

:deep(.el-divider--horizontal) {
  margin: 16px 0;
}

:deep(.el-divider__text) {
  font-weight: normal;
  color: #606266;
}
</style>
