<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">五子棋在线对弈</h1>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <input 
            v-model="username" 
            type="text" 
            placeholder="用户名"
            required
            minlength="3"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="password" 
            type="password" 
            placeholder="密码"
            required
            minlength="8"
          />
        </div>
        <button type="submit" class="btn-primary">登录</button>
      </form>
      <div class="links">
        <router-link to="/register">没有账号？立即注册</router-link>
      </div>
      <div v-if="error" class="error">{{ error }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')

async function handleLogin() {
  try {
    error.value = ''
    const success = await authStore.login(username.value, password.value)
    if (success) {
      router.push('/lobby')
    } else {
      error.value = '登录失败，请检查用户名和密码'
    }
  } catch (err) {
    error.value = err.response?.data?.message || '登录失败'
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 15px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
}

.title {
  text-align: center;
  color: #667eea;
  margin-bottom: 30px;
  font-size: 28px;
}

.form-group {
  margin-bottom: 20px;
}

input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #667eea;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  transition: transform 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
}

.links {
  margin-top: 20px;
  text-align: center;
}

.links a {
  color: #667eea;
  text-decoration: none;
}

.links a:hover {
  text-decoration: underline;
}

.error {
  margin-top: 15px;
  padding: 10px;
  background: #fee;
  border-radius: 5px;
  color: #c33;
  text-align: center;
}
</style>
