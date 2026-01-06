<template>
  <div class="game-container">
    <div class="game-header">
      <h2>对局中...</h2>
      <button @click="goBack" class="btn-back">返回大厅</button>
    </div>
    <div class="game-board">
      <p>游戏界面开发中</p>
      <p>游戏ID: {{ $route.params.gameId }}</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goBack() {
  router.push('/lobby')
}
</script>

<style scoped>
.game-container {
  min-height: 100vh;
  padding: 20px;
}

.game-header {
  background: white;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-back {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border-radius: 8px;
  font-weight: bold;
}

.game-board {
  background: white;
  border-radius: 15px;
  padding: 40px;
  text-align: center;
  min-height: 500px;
}
</style>
