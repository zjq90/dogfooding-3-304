<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="library-pattern"></div>
    </div>
    
    <div class="login-box">
      <div class="login-header">
        <div class="logo">
          <i class="el-icon-reading"></i>
        </div>
        <h1 class="title">图书借阅管理系统</h1>
        <p class="subtitle">Library Management System</p>
      </div>
      
      <el-form 
        ref="loginForm" 
        :model="loginForm" 
        :rules="loginRules" 
        class="login-form"
        @keyup.enter.native="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="el-icon-user"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="el-icon-lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            :loading="loading"
            type="primary"
            size="large"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>默认账号: admin / 密码: admin123</p>
      </div>
    </div>
    
    <div class="decoration">
      <div class="book book-1"></div>
      <div class="book book-2"></div>
      <div class="book book-3"></div>
    </div>
  </div>
</template>

<script>
import { login } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: 'admin',
        password: 'admin123'
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async valid => {
        if (valid) {
          this.loading = true
          try {
            const res = await login(this.loginForm)
            if (res.code === 200) {
              const { token, userId, username, realName, role, avatar } = res.data
              const userInfo = { userId, username, realName, role, avatar }
              this.$store.dispatch('login', { token, userInfo })
              this.$message.success('登录成功')
              this.$router.push('/')
            }
          } catch (error) {
            console.error('登录失败:', error)
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E3F2FD 0%, #BBDEFB 50%, #E8F5E9 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
}

.library-pattern {
  width: 100%;
  height: 100%;
  background-image: 
    repeating-linear-gradient(90deg, transparent, transparent 50px, rgba(91, 143, 249, 0.1) 50px, rgba(91, 143, 249, 0.1) 51px),
    repeating-linear-gradient(0deg, transparent, transparent 50px, rgba(91, 143, 249, 0.1) 50px, rgba(91, 143, 249, 0.1) 51px);
}

.login-box {
  width: 420px;
  padding: 50px 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 10;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #5B8FF9 0%, #5AD8A6 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(91, 143, 249, 0.3);
}

.logo i {
  font-size: 40px;
  color: #fff;
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 14px;
  color: #8C8C8C;
  letter-spacing: 1px;
}

.login-form {
  margin-top: 30px;
}

.login-form :deep(.el-input__inner) {
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
  border: 1px solid #E8E8E8;
  transition: all 0.3s;
}

.login-form :deep(.el-input__inner:focus) {
  border-color: #5B8FF9;
  box-shadow: 0 0 0 2px rgba(91, 143, 249, 0.2);
}

.login-form :deep(.el-input__prefix) {
  left: 15px;
}

.login-form :deep(.el-input__icon) {
  font-size: 18px;
  color: #8C8C8C;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #5B8FF9 0%, #5AD8A6 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(91, 143, 249, 0.3);
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(91, 143, 249, 0.4);
}

.login-footer {
  margin-top: 30px;
  text-align: center;
  color: #8C8C8C;
  font-size: 13px;
}

/* 装饰元素 */
.decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.book {
  position: absolute;
  border-radius: 4px;
  opacity: 0.15;
}

.book-1 {
  width: 60px;
  height: 80px;
  background: #5B8FF9;
  top: 15%;
  left: 10%;
  transform: rotate(-15deg);
}

.book-2 {
  width: 50px;
  height: 70px;
  background: #5AD8A6;
  top: 70%;
  right: 15%;
  transform: rotate(20deg);
}

.book-3 {
  width: 70px;
  height: 90px;
  background: #F6BD16;
  bottom: 20%;
  left: 15%;
  transform: rotate(-10deg);
}

/* 响应式 */
@media (max-width: 480px) {
  .login-box {
    width: 90%;
    padding: 40px 30px;
  }
  
  .title {
    font-size: 24px;
  }
}
</style>
