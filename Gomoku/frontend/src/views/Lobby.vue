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
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVE('MEDIUM')">
          <h2>🎯 人机对战</h2>
          <p>中等难度</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVE('HARD')">
          <h2>🔥 人机对战</h2>
          <p>困难难度</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVP">
          <h2>⚔️ 在线对弈</h2>
          <p>匹配真实玩家</p>
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function startPVE(difficulty) {
  alert(`人机对战功能开发中（难度: ${difficulty}）`)
  // TODO: 实现人机对战
}

function startPVP() {
  alert('在线对弈功能开发中')
  // TODO: 实现匹配功能
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.lobby-container {
  min-height: 100vh;
  padding: 20px;
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
  margin-bottom: 20px;
}

.btn-mode {
  padding: 10px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  font-weight: bold;
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
</style>
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
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVE('MEDIUM')">
          <h2>🎯 人机对战</h2>
          <p>中等难度</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVE('HARD')">
          <h2>🔥 人机对战</h2>
          <p>困难难度</p>
          <button class="btn-mode">开始游戏</button>
        </div>
        
        <div class="mode-card" @click="startPVP">
          <h2>⚔️ 在线对弈</h2>
          <p>匹配真实玩家</p>
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
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function startPVE(difficulty) {
  alert(`人机对战功能开发中（难度: ${difficulty}）`)
  // TODO: 实现人机对战
}

function startPVP() {
  alert('在线对弈功能开发中')
  // TODO: 实现匹配功能
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.lobby-container {
  min-height: 100vh;
  padding: 20px;
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
  margin-bottom: 20px;
}

.btn-mode {
  padding: 10px 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  font-weight: bold;
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
</style>
