<template>
  <div class="lobby-container">
    <div class="header">
      <h1>五子棋对弈大厅</h1>
      <div class="user-info">
        <span>{{ authStore.user?.username }}</span>
        <span>等级: {{ authStore.user?.level }}</span>
        <span>积分: {{ authStore.user?.score }}</span>
        <button @click="handleLogout" class="btn-logout">退出</button>
      </div>
    </div>
    
    <div class="main-content">
      <div class="game-modes">
        <div class="mode-card" @click="startPVE('EASY')">
          <h2>🎮 人机对战</h2>
          <p>简单难度</p>
          <p class="desc">适合新手练习</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVE('MEDIUM')">
          <h2>🎯 人机对战</h2>
          <p>中等难度</p>
          <p class="desc">有一定挑战性</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVE('HARD')">
          <h2>🔥 人机对战</h2>
          <p>困难难度</p>
          <p class="desc">AI使用MinMax算法</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVP">
          <h2>⚔️ 在线对弈</h2>
          <p>匹配真实玩家</p>
          <p class="desc">根据等级匹配对手</p>
          <button class="btn-mode">开始匹配</button>
        </div>
      </div>
      
      <div class="quick-links">
        <router-link to="/ranking" class="link-card">
          <h3>🏆 排行榜</h3>
          <p>查看高手榜单</p>
        </router-link>
      </div>
    </div>
    
    <!-- 匹配中弹窗 -->
    <div v-if="isMatching" class="matching-overlay">
      <div class="matching-modal">
        <div class="matching-animation">
          <div class="matching-circle"></div>
          <div class="matching-circle delay"></div>
        </div>
        <h2>正在匹配对手...</h2>
        <p>等待时间: {{ matchingTime }}秒</p>
        <p class="tip">正在寻找等级相近的玩家</p>
        <button @click="cancelMatch" class="btn-cancel">取消匹配</button>
      </div>
    </div>
    
    <!-- 加载遮罩 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>{{ loadingText }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../utils/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
const loadingText = ref('处理中...')
const isMatching = ref(false)
const matchingTime = ref(0)
let matchingTimer = null
let pollTimer = null

// 人机对战
async function startPVE(difficulty) {
  loading.value = true
  loadingText.value = '创建对局中...'
  
  try {
    const res = await api.post('/api/game/create', { difficulty })
    const game = res.data.data
    router.push(`/game/${game.gameId}`)
  } catch (error) {
    console.error('创建对局失败:', error)
    alert(error.response?.data?.message || '创建对局失败')
  } finally {
    loading.value = false
  }
}

// 在线匹配
async function startPVP() {
  isMatching.value = true
  matchingTime.value = 0
  
  try {
    const res = await api.post('/api/match/start')
    const result = res.data.data
    
    if (result.matched) {
      // 直接匹配成功
      isMatching.value = false
      router.push(`/game/${result.gameId}`)
      return
    }
    
    // 开始计时和轮询
    startMatchingTimer()
    startPolling()
  } catch (error) {
    console.error('开始匹配失败:', error)
    alert(error.response?.data?.message || '开始匹配失败')
    isMatching.value = false
  }
}

// 取消匹配
async function cancelMatch() {
  try {
    await api.post('/api/match/cancel')
  } catch (error) {
    console.error('取消匹配失败:', error)
  }
  
  stopTimers()
  isMatching.value = false
}

// 开始计时
function startMatchingTimer() {
  matchingTimer = setInterval(() => {
    matchingTime.value++
    
    // 超过120秒自动取消
    if (matchingTime.value >= 120) {
      alert('匹配超时，请稍后重试')
      cancelMatch()
    }
  }, 1000)
}

// 轮询匹配状态
function startPolling() {
  pollTimer = setInterval(async () => {
    try {
      const res = await api.get('/api/match/status')
      const status = res.data.data
      
      if (status.matched && status.gameId) {
        stopTimers()
        isMatching.value = false
        router.push(`/game/${status.gameId}`)
      }
    } catch (error) {
      console.error('查询匹配状态失败:', error)
    }
  }, 2000) // 每2秒轮询一次
}

// 停止所有计时器
function stopTimers() {
  if (matchingTimer) {
    clearInterval(matchingTimer)
    matchingTimer = null
  }
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

// 处理路由参数（从游戏页面返回时自动开始）
onMounted(() => {
  if (route.query.action === 'pve' && route.query.difficulty) {
    startPVE(route.query.difficulty)
  } else if (route.query.action === 'pvp') {
    startPVP()
  }
})

onUnmounted(() => {
  stopTimers()
})
</script>

<style scoped>
.lobby-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  background: white;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.header h1 {
  color: #667eea;
  font-size: 28px;
  margin: 0;
}

.user-info {
  display: flex;
  gap: 20px;
  align-items: center;
}

.user-info span {
  color: #333;
  font-weight: 500;
}

.btn-logout {
  padding: 8px 20px;
  background: #ff5252;
  color: white;
  border-radius: 5px;
  font-weight: bold;
  border: none;
  cursor: pointer;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
}

.game-modes {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.mode-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.mode-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.mode-card h2 {
  color: #667eea;
  margin-bottom: 10px;
  font-size: 24px;
}

.mode-card p {
  color: #666;
  margin-bottom: 5px;
}

.mode-card .desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 20px;
}

.btn-mode {
  padding: 10px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  font-weight: bold;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;
}

.btn-mode:hover {
  transform: scale(1.05);
}

.quick-links {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.link-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  text-align: center;
  text-decoration: none;
  transition: transform 0.3s;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
  min-width: 200px;
}

.link-card:hover {
  transform: translateY(-5px);
}

.link-card h3 {
  color: #667eea;
  margin-bottom: 10px;
}

.link-card p {
  color: #666;
}

/* 匹配中弹窗 */
.matching-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.matching-modal {
  background: white;
  border-radius: 20px;
  padding: 50px;
  text-align: center;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.matching-animation {
  position: relative;
  width: 100px;
  height: 100px;
  margin: 0 auto 30px;
}

.matching-circle {
  position: absolute;
  width: 100%;
  height: 100%;
  border: 4px solid #667eea;
  border-radius: 50%;
  animation: ripple 2s infinite;
}

.matching-circle.delay {
  animation-delay: 1s;
}

@keyframes ripple {
  0% { transform: scale(0.3); opacity: 1; }
  100% { transform: scale(1.5); opacity: 0; }
}

.matching-modal h2 {
  color: #667eea;
  margin-bottom: 15px;
}

.matching-modal p {
  color: #666;
  margin-bottom: 10px;
}

.matching-modal .tip {
  color: #999;
  font-size: 14px;
  margin-bottom: 30px;
}

.btn-cancel {
  padding: 12px 40px;
  background: #f5f5f5;
  color: #666;
  border-radius: 8px;
  font-weight: bold;
  border: none;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

/* 加载遮罩 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 200;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-overlay p {
  color: #667eea;
  font-weight: bold;
}
</style>
