<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="title">注册账号</h1>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <input 
            v-model="username" 
            type="text" 
            placeholder="用户名（3-50字符）"
            required
            minlength="3"
            maxlength="50"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="email" 
            type="email" 
            placeholder="邮箱（可选）"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="password" 
            type="password" 
            placeholder="密码（至少8位）"
            required
            minlength="8"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="confirmPassword" 
            type="password" 
            placeholder="确认密码"
            required
          />
        </div>
        <button type="submit" class="btn-primary">注册</button>
      </form>
      <div class="links">
        <router-link to="/login">已有账号？立即登录</router-link>
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
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')

async function handleRegister() {
  try {
    error.value = ''
    
    if (password.value !== confirmPassword.value) {
      error.value = '两次输入的密码不一致'
      return
    }
    
    const success = await authStore.register(username.value, password.value, email.value)
    if (success) {
      router.push('/lobby')
    } else {
      error.value = '注册失败'
    }
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败'
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.register-card {
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
<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="title">注册账号</h1>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <input 
            v-model="username" 
            type="text" 
            placeholder="用户名（3-50字符）"
            required
            minlength="3"
            maxlength="50"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="email" 
            type="email" 
            placeholder="邮箱（可选）"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="password" 
            type="password" 
            placeholder="密码（至少8位）"
            required
            minlength="8"
          />
        </div>
        <div class="form-group">
          <input 
            v-model="confirmPassword" 
            type="password" 
            placeholder="确认密码"
            required
          />
        </div>
        <button type="submit" class="btn-primary">注册</button>
      </form>
      <div class="links">
        <router-link to="/login">已有账号？立即登录</router-link>
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
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')

async function handleRegister() {
  try {
    error.value = ''
    
    if (password.value !== confirmPassword.value) {
      error.value = '两次输入的密码不一致'
      return
    }
    
    const success = await authStore.register(username.value, password.value, email.value)
    if (success) {
      router.push('/lobby')
    } else {
      error.value = '注册失败'
    }
  } catch (err) {
    error.value = err.response?.data?.message || '注册失败'
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.register-card {
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
