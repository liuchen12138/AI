<template>
  <div class="ranking-container">
    <div class="header">
      <h1>🏆 排行榜</h1>
      <router-link to="/lobby" class="btn-back">返回大厅</router-link>
    </div>
    
    <div class="ranking-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.value"
        :class="['tab-button', { active: currentTab === tab.value }]"
        @click="currentTab = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>
    
    <div class="ranking-list">
      <div class="list-header">
        <span class="rank-col">排名</span>
        <span class="name-col">用户名</span>
        <span class="level-col">等级</span>
        <span class="score-col">积分</span>
        <span class="winrate-col">胜率</span>
      </div>
      
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="rankings.length === 0" class="empty">暂无数据</div>
      <div v-else class="list-body">
        <div v-for="(user, index) in rankings" :key="user.userId" class="list-item">
          <span class="rank-col">{{ index + 1 }}</span>
          <span class="name-col">{{ user.username }}</span>
          <span class="level-col">Lv.{{ user.level }}</span>
          <span class="score-col">{{ user.score }}</span>
          <span class="winrate-col">{{ user.winRate?.toFixed(1) }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import api from '../utils/api'

const tabs = [
  { label: '积分榜', value: 'score' },
  { label: '胜率榜', value: 'winrate' },
  { label: '等级榜', value: 'level' }
]

const currentTab = ref('score')
const rankings = ref([])
const loading = ref(false)

async function fetchRankings() {
  loading.value = true
  try {
    const response = await api.get(`/api/ranking/${currentTab.value}`)
    if (response.data.success) {
      rankings.value = response.data.data
    }
  } catch (error) {
    console.error('获取排行榜失败:', error)
  } finally {
    loading.value = false
  }
}

watch(currentTab, fetchRankings)
onMounted(fetchRankings)
</script>

<style scoped>
.ranking-container {
  min-height: 100vh;
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.header {
  background: white;
  border-radius: 15px;
  padding: 20px 30px;
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header h1 {
  color: #667eea;
}

.btn-back {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border-radius: 8px;
  text-decoration: none;
  font-weight: bold;
}

.ranking-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-button {
  flex: 1;
  padding: 15px;
  background: white;
  border-radius: 10px;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s;
}

.tab-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.ranking-list {
  background: white;
  border-radius: 15px;
  padding: 20px;
}

.list-header, .list-item {
  display: grid;
  grid-template-columns: 80px 1fr 100px 100px 100px;
  gap: 10px;
  padding: 15px;
  align-items: center;
}

.list-header {
  font-weight: bold;
  color: #667eea;
  border-bottom: 2px solid #e0e0e0;
}

.list-item {
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.2s;
}

.list-item:hover {
  background: #f9f9f9;
}

.rank-col {
  text-align: center;
  font-weight: bold;
  color: #667eea;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>
