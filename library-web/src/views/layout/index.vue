<template>
  <el-container class="layout-container">
    <!-- 顶部导航 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <i class="el-icon-reading"></i>
          <span class="logo-text">图书借阅管理系统</span>
        </div>
      </div>
      
      <div class="header-center">
        <el-menu
          :default-active="activeMenu"
          class="nav-menu"
          mode="horizontal"
          background-color="transparent"
          text-color="#595959"
          active-text-color="#5B8FF9"
          router
        >
          <el-menu-item 
            v-for="item in menuList" 
            :key="item.path" 
            :index="item.path"
          >
            <i :class="item.icon"></i>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" :src="userInfo.avatar || defaultAvatar"></el-avatar>
            <span class="username">{{ userInfo.realName || userInfo.username }}</span>
            <i class="el-icon-arrow-down"></i>
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile">
              <i class="el-icon-user"></i> 个人中心
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <i class="el-icon-lock"></i> 修改密码
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <i class="el-icon-switch-button"></i> 退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主内容区 -->
    <el-main class="main-content">
      <router-view />
    </el-main>
  </el-container>
</template>

<script>
import { mapGetters } from 'vuex'
import { logout } from '@/api/auth'

export default {
  name: 'Layout',
  data() {
    return {
      defaultAvatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      menuList: [
        { path: '/dashboard', title: '数据概览', icon: 'el-icon-s-data' },
        { path: '/books', title: '图书管理', icon: 'el-icon-reading' },
        { path: '/categories', title: '分类管理', icon: 'el-icon-folder-opened' },
        { path: '/borrow', title: '借阅管理', icon: 'el-icon-document' },
        { path: '/users', title: '用户管理', icon: 'el-icon-user' }
      ]
    }
  },
  computed: {
    ...mapGetters(['userInfo']),
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$message.info('个人中心功能开发中')
          break
        case 'password':
          this.$message.info('修改密码功能开发中')
          break
        case 'logout':
          this.handleLogout()
          break
      }
    },
    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await logout()
        this.$store.dispatch('logout')
        this.$message.success('退出成功')
        this.$router.push('/login')
      } catch (error) {
        // 用户取消
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
  background-color: #F5F7FA;
}

.header {
  height: 64px !important;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo i {
  font-size: 32px;
  color: #5B8FF9;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  color: #262626;
  letter-spacing: 1px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border-bottom: none;
}

.nav-menu :deep(.el-menu-item) {
  font-size: 15px;
  height: 64px;
  line-height: 64px;
  padding: 0 24px;
  margin: 0 4px;
  border-bottom: none !important;
  transition: all 0.3s;
}

.nav-menu :deep(.el-menu-item:hover) {
  background-color: rgba(91, 143, 249, 0.05) !important;
}

.nav-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(91, 143, 249, 0.1) !important;
  font-weight: 500;
}

.nav-menu :deep(.el-menu-item i) {
  margin-right: 6px;
  font-size: 18px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 20px;
  transition: all 0.3s;
}

.user-info:hover {
  background-color: #F5F7FA;
}

.username {
  font-size: 14px;
  color: #595959;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  margin-top: 64px;
  padding: 24px;
  min-height: calc(100vh - 64px);
}
</style>
