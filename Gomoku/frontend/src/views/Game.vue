<template>
  <div class="game-container">
    <div class="game-header">
      <h2>{{ gameTitle }}</h2>
      <div class="game-info">
        <span v-if="game" class="mode-badge">{{ game.gameMode === 'PVE' ? '人机对战' : '在线对弈' }}</span>
        <span v-if="game && game.aiDifficulty" class="difficulty-badge">{{ difficultyText }}</span>
      </div>
      <button @click="goBack" class="btn-back">返回大厅</button>
    </div>
    
    <div class="game-main">
      <!-- 左侧玩家信息 -->
      <div class="player-panel left">
        <div :class="['player-card', { active: currentTurn === 'BLACK' }]">
          <div class="player-stone black"></div>
          <div class="player-name">{{ blackPlayerName }}</div>
          <div v-if="currentTurn === 'BLACK'" class="turn-indicator">思考中...</div>
        </div>
      </div>
      
      <!-- 棋盘 -->
      <div class="board-container">
        <div class="board" @click="handleBoardClick">
          <!-- 棋盘线条 -->
          <svg class="board-lines" viewBox="0 0 580 580">
            <!-- 横线 -->
            <line v-for="i in 15" :key="'h' + i" 
                  :x1="20" :y1="20 + (i-1) * 40" 
                  :x2="580" :y2="20 + (i-1) * 40" 
                  stroke="#8B4513" stroke-width="1"/>
            <!-- 竖线 -->
            <line v-for="i in 15" :key="'v' + i" 
                  :x1="20 + (i-1) * 40" :y1="20" 
                  :x2="20 + (i-1) * 40" :y2="580" 
                  stroke="#8B4513" stroke-width="1"/>
            <!-- 星位 -->
            <circle v-for="star in starPoints" :key="'star' + star.x + star.y" 
                    :cx="20 + star.x * 40" :cy="20 + star.y * 40" 
                    r="4" fill="#8B4513"/>
          </svg>
          
          <!-- 棋子 -->
          <div v-for="(move, index) in moves" :key="index"
               :class="['stone', move.color]"
               :style="{ left: (20 + move.x * 40 - 15) + 'px', top: (20 + move.y * 40 - 15) + 'px' }">
            <span v-if="showMoveNumber" class="move-number">{{ index + 1 }}</span>
          </div>
          
          <!-- 悬浮预览 -->
          <div v-if="hoverPosition && canMove"
               :class="['stone', 'preview', myColor]"
               :style="{ left: (20 + hoverPosition.x * 40 - 15) + 'px', top: (20 + hoverPosition.y * 40 - 15) + 'px' }">
          </div>
          
          <!-- 最后一手标记 -->
          <div v-if="lastMove"
               class="last-move-marker"
               :style="{ left: (20 + lastMove.x * 40) + 'px', top: (20 + lastMove.y * 40) + 'px' }">
          </div>
        </div>
      </div>
      
      <!-- 右侧玩家信息 -->
      <div class="player-panel right">
        <div :class="['player-card', { active: currentTurn === 'WHITE' }]">
          <div class="player-stone white"></div>
          <div class="player-name">{{ whitePlayerName }}</div>
          <div v-if="currentTurn === 'WHITE'" class="turn-indicator">思考中...</div>
        </div>
      </div>
    </div>
    
    <!-- 游戏控制 -->
    <div class="game-controls">
      <button @click="toggleMoveNumber" class="btn-control">
        {{ showMoveNumber ? '隐藏手数' : '显示手数' }}
      </button>
      <button @click="handleResign" class="btn-resign" :disabled="gameOver || loading">
        认输
      </button>
    </div>
    
    <!-- 游戏结束提示，显示在棋盘上方 -->
    <div v-if="gameOver" class="game-over-message">
      <h2>{{ gameResultText }}</h2>
      <p>{{ gameResultDescription }}</p>
      <div class="modal-buttons">
        <button @click="playAgain" class="btn-primary">再来一局</button>
        <button @click="goBack" class="btn-secondary">返回大厅</button>
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
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import api from '../utils/api'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 游戏数据
const game = ref(null)
const board = ref([])
const moves = ref([])
const currentTurn = ref('BLACK')
const gameOver = ref(false)
const winnerId = ref(null)
const loading = ref(true)
const loadingText = ref('加载中...')
const showMoveNumber = ref(false)
const hoverPosition = ref(null)
let pollTimer = null // 在线对弈轮询定时器

// 星位坐标
const starPoints = [
  { x: 3, y: 3 }, { x: 11, y: 3 }, { x: 7, y: 7 },
  { x: 3, y: 11 }, { x: 11, y: 11 }
]

// 计算属性
const gameId = computed(() => route.params.gameId)

const myColor = computed(() => {
  if (!game.value || !authStore.user) return null
  return game.value.blackPlayerId === authStore.user.userId ? 'black' : 'white'
})

const canMove = computed(() => {
  if (gameOver.value || loading.value) return false
  if (!game.value) return false
  const isMyTurn = (currentTurn.value === 'BLACK' && myColor.value === 'black') ||
                   (currentTurn.value === 'WHITE' && myColor.value === 'white')
  return isMyTurn
})

const blackPlayerName = computed(() => {
  if (!game.value) return '黑方'
  if (game.value.gameMode === 'PVE') {
    return game.value.blackPlayerId ? '我' : 'AI'
  }
  return game.value.blackPlayerId === authStore.user?.userId ? '我' : '对手'
})

const whitePlayerName = computed(() => {
  if (!game.value) return '白方'
  if (game.value.gameMode === 'PVE') {
    return game.value.whitePlayerId ? '我' : 'AI'
  }
  return game.value.whitePlayerId === authStore.user?.userId ? '我' : '对手'
})

const lastMove = computed(() => {
  if (moves.value.length === 0) return null
  return moves.value[moves.value.length - 1]
})

const gameTitle = computed(() => {
  if (gameOver.value) return '对局结束'
  if (!canMove.value && !loading.value) return '等待对手...'
  return '轮到您落子'
})

const difficultyText = computed(() => {
  if (!game.value?.aiDifficulty) return ''
  const map = { EASY: '简单', MEDIUM: '中等', HARD: '困难' }
  return map[game.value.aiDifficulty] || ''
})

const gameResultText = computed(() => {
  if (!gameOver.value) return ''
  if (winnerId.value === null) return '平局'
  const isWin = winnerId.value === authStore.user?.userId ||
                (game.value?.gameMode === 'PVE' && 
                 ((winnerId.value === game.value?.blackPlayerId && myColor.value === 'black') ||
                  (winnerId.value === game.value?.whitePlayerId && myColor.value === 'white')))
  return isWin ? '恭喜获胜！' : '很遗憾，您输了'
})

const gameResultDescription = computed(() => {
  if (!gameOver.value) return ''
  return `本局共 ${moves.value.length} 手`
})

// 方法
async function loadGame() {
  loading.value = true
  loadingText.value = '加载对局...'
  
  try {
    // 获取对局信息
    const gameRes = await api.get(`/api/game/${gameId.value}`)
    game.value = gameRes.data.data
    
    // 获取棋盘状态
    const boardRes = await api.get(`/api/game/${gameId.value}/board`)
    const state = boardRes.data.data
    
    board.value = state.board
    currentTurn.value = state.currentTurn
    
    // 处理棋谱
    moves.value = state.moves.map(m => ({
      x: m.positionX,
      y: m.positionY,
      color: getColorByMoveNumber(m.moveNumber)
    }))
    
    // 检查游戏状态
    if (state.gameStatus !== 'PLAYING') {
      gameOver.value = true
      winnerId.value = state.winnerId
    } else if (game.value.gameMode === 'PVP') {
      // 在线对弈模式，启动轮询
      startPolling()
    }
  } catch (error) {
    console.error('加载对局失败:', error)
    alert('加载对局失败: ' + (error.response?.data?.message || error.message))
    router.push('/lobby')
  } finally {
    loading.value = false
  }
}

function getColorByMoveNumber(moveNumber) {
  return moveNumber % 2 === 1 ? 'black' : 'white'
}

async function handleBoardClick(event) {
  if (!canMove.value) return
  
  const rect = event.currentTarget.getBoundingClientRect()
  const x = Math.round((event.clientX - rect.left - 20) / 40)
  const y = Math.round((event.clientY - rect.top - 20) / 40)
  
  if (x < 0 || x > 14 || y < 0 || y > 14) return
  if (board.value[x][y] !== 0) return
  
  await makeMove(x, y)
}

async function makeMove(x, y) {
  loading.value = true
  loadingText.value = '落子中...'
  
  try {
    const res = await api.post(`/api/game/${gameId.value}/move`, { x, y })
    const result = res.data.data
    
    // 添加玩家落子
    moves.value.push({ x, y, color: myColor.value })
    board.value[x][y] = myColor.value === 'black' ? 1 : 2
    
    if (result.gameOver) {
      gameOver.value = true
      winnerId.value = result.winnerId
      stopPolling()
    } else {
      currentTurn.value = result.nextTurn
      
      // 处理AI落子（仅人机对战）
      if (result.aiMove) {
        loadingText.value = 'AI思考中...'
        await new Promise(resolve => setTimeout(resolve, 500))
        
        const aiColor = myColor.value === 'black' ? 'white' : 'black'
        moves.value.push({ x: result.aiMove.x, y: result.aiMove.y, color: aiColor })
        board.value[result.aiMove.x][result.aiMove.y] = aiColor === 'black' ? 1 : 2
        
        // 重新获取游戏状态检查AI是否获胜
        const boardRes = await api.get(`/api/game/${gameId.value}/board`)
        const state = boardRes.data.data
        currentTurn.value = state.currentTurn
        
        if (state.gameStatus !== 'PLAYING') {
          gameOver.value = true
          winnerId.value = state.winnerId
        }
      }
    }
  } catch (error) {
    console.error('落子失败:', error)
    alert(error.response?.data?.message || '落子失败')
  } finally {
    loading.value = false
  }
}

// 在线对弈轮询
function startPolling() {
  if (pollTimer) return
  pollTimer = setInterval(async () => {
    if (gameOver.value || loading.value) return
    
    try {
      const boardRes = await api.get(`/api/game/${gameId.value}/board`)
      const state = boardRes.data.data
      
      // 检查是否有新落子
      if (state.moves.length > moves.value.length) {
        // 同步新的落子
        for (let i = moves.value.length; i < state.moves.length; i++) {
          const m = state.moves[i]
          moves.value.push({
            x: m.positionX,
            y: m.positionY,
            color: getColorByMoveNumber(m.moveNumber)
          })
          board.value[m.positionX][m.positionY] = m.moveNumber % 2 === 1 ? 1 : 2
        }
        currentTurn.value = state.currentTurn
      }
      
      // 检查游戏状态
      if (state.gameStatus !== 'PLAYING') {
        gameOver.value = true
        winnerId.value = state.winnerId
        stopPolling()
      }
    } catch (error) {
      console.error('轮询失败:', error)
    }
  }, 1500) // 每1.5秒轮询一次
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

async function handleResign() {
  if (!confirm('确定要认输吗？')) return
  
  loading.value = true
  loadingText.value = '处理中...'
  
  try {
    await api.post(`/api/game/${gameId.value}/resign`)
    gameOver.value = true
    winnerId.value = myColor.value === 'black' ? game.value.whitePlayerId : game.value.blackPlayerId
    stopPolling()
  } catch (error) {
    console.error('认输失败:', error)
    alert(error.response?.data?.message || '操作失败')
  } finally {
    loading.value = false
  }
}

function toggleMoveNumber() {
  showMoveNumber.value = !showMoveNumber.value
}

function goBack() {
  router.push('/lobby')
}

function playAgain() {
  // 重新创建同类型对局
  if (game.value?.gameMode === 'PVE') {
    router.push({ path: '/lobby', query: { action: 'pve', difficulty: game.value.aiDifficulty } })
  } else {
    router.push({ path: '/lobby', query: { action: 'pvp' } })
  }
}



function handleMouseMove(event) {
  if (!canMove.value) {
    hoverPosition.value = null
    return
  }
  
  const boardEl = document.querySelector('.board')
  if (!boardEl) return
  
  const rect = boardEl.getBoundingClientRect()
  const x = Math.round((event.clientX - rect.left - 20) / 40)
  const y = Math.round((event.clientY - rect.top - 20) / 40)
  
  if (x >= 0 && x <= 14 && y >= 0 && y <= 14 && board.value[x]?.[y] === 0) {
    hoverPosition.value = { x, y }
  } else {
    hoverPosition.value = null
  }
}

// 生命周期
onMounted(() => {
  loadGame()
  document.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleMouseMove)
  stopPolling()
})
</script>

<style scoped>
.game-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.game-header {
  background: white;
  border-radius: 15px;
  padding: 15px 30px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.1);
}

.game-header h2 {
  color: #667eea;
  margin: 0;
}

.game-info {
  display: flex;
  gap: 10px;
}

.mode-badge, .difficulty-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
}

.mode-badge {
  background: #e3f2fd;
  color: #1976d2;
}

.difficulty-badge {
  background: #fff3e0;
  color: #e65100;
}

.btn-back {
  padding: 8px 20px;
  background: #667eea;
  color: white;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.game-main {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 30px;
}

.player-panel {
  width: 150px;
}

.player-card {
  background: white;
  border-radius: 15px;
  padding: 20px;
  text-align: center;
  transition: all 0.3s;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.player-card.active {
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
  border: 2px solid #667eea;
}

.player-stone {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin: 0 auto 10px;
}

.player-stone.black {
  background: radial-gradient(circle at 30% 30%, #555, #000);
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.3);
}

.player-stone.white {
  background: radial-gradient(circle at 30% 30%, #fff, #ddd);
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
  border: 1px solid #ccc;
}

.player-name {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.turn-indicator {
  margin-top: 10px;
  color: #667eea;
  font-size: 14px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.board-container {
  background: white;
  border-radius: 15px;
  padding: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.board {
  width: 580px;
  height: 580px;
  background: #dcb35c;
  position: relative;
  cursor: pointer;
  border-radius: 5px;
}

.board-lines {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
}

.stone {
  position: absolute;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: transform 0.1s;
}

.stone.black {
  background: radial-gradient(circle at 30% 30%, #555, #000);
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.4);
}

.stone.white {
  background: radial-gradient(circle at 30% 30%, #fff, #ddd);
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.3);
  border: 1px solid #aaa;
}

.stone.preview {
  opacity: 0.5;
  z-index: 5;
}

.move-number {
  font-size: 12px;
  font-weight: bold;
}

.stone.black .move-number {
  color: white;
}

.stone.white .move-number {
  color: black;
}

.last-move-marker {
  position: absolute;
  width: 34px;
  height: 34px;
  border: 2px solid #ff0000;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  z-index: 20;
  box-shadow: 0 0 10px rgba(255, 0, 0, 0.7);
  animation: pulse 1s infinite;
}

.game-over-message {
  position: absolute;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  padding: 20px 40px;
  border-radius: 15px;
  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.3);
  z-index: 30;
  text-align: center;
  animation: slideDown 0.5s ease;
}

@keyframes slideDown {
  from { top: -50px; opacity: 0; }
  to { top: 20px; opacity: 1; }
}

.game-over-message h2 {
  color: #667eea;
  margin: 0 0 10px 0;
  font-size: 28px;
}

.game-over-message p {
  color: #666;
  margin: 0 0 20px 0;
}

.game-over-message .modal-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.game-controls {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.btn-control, .btn-resign {
  padding: 10px 30px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  border: none;
  transition: all 0.3s;
}

.btn-control {
  background: white;
  color: #667eea;
}

.btn-resign {
  background: #ff5252;
  color: white;
}

.btn-resign:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.game-over-overlay {
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

.game-over-modal {
  background: white;
  border-radius: 20px;
  padding: 40px 60px;
  text-align: center;
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.game-over-modal h2 {
  color: #667eea;
  font-size: 32px;
  margin-bottom: 10px;
}

.game-over-modal p {
  color: #666;
  margin-bottom: 30px;
}

.modal-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-primary, .btn-secondary {
  padding: 12px 30px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

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
