<template>
  <div id="app">
    <!-- 提示框 -->
    <div class="floating-alert-view">
      <el-alert v-if="showAlert && alertType === 'success'" :title="alertTitle" type="success"
        :description="alertDescription" show-icon class="floating-alert"></el-alert>
      <el-alert v-if="showAlert && alertType === 'info'" :title="alertTitle" type="info" :description="alertDescription"
        show-icon class="floating-alert"></el-alert>
      <el-alert v-if="showAlert && alertType === 'warning'" :title="alertTitle" type="warning"
        :description="alertDescription" show-icon class="floating-alert"></el-alert>
      <el-alert v-if="showAlert && alertType === 'error'" :title="alertTitle" type="error"
        :description="alertDescription" show-icon class="floating-alert"></el-alert>
    </div>
    <!-- 非登录界面 -->
    <template v-if="!isAuthPage">
      <!-- 标题 -->
      <el-header>
        <h2>个人云盘系统</h2>
      </el-header>
      <!-- 侧栏和内容页面 -->
      <el-container>
        <transition name="sidebar-fade">
          <el-menu :collapse-transition="false" default-active="1-1" class="sidebar" :default-openeds="defaultOpeneds"
            @select="handleSelect" :collapse="isCollapsed">
            <el-menu-item index="0" @click="toggleCollapse">
              <i class="el-icon-menu"></i>
              <span class="no-select" slot="title"><strong>{{ isCollapsed? '展开菜单' : '收缩菜单' }}</strong></span>
            </el-menu-item>
            <el-submenu index="1">
              <template slot="title">
                <i class="el-icon-folder"></i>
                <span class="no-select"
                  slot="title"><strong>我的文件</strong>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
              </template>
              <el-menu-item index="1-1" @click="goToPage('all')">
                <i class="el-icon-document"></i>
                <span class="no-select" slot="title">全部文件</span>
              </el-menu-item>
              <el-menu-item index="1-2" @click="goToPage('pic')">
                <i class="el-icon-picture"></i>
                <span class="no-select" slot="title">图片文件</span>
              </el-menu-item>
              <el-menu-item index="1-3" @click="goToPage('vid')">
                <i class="el-icon-video-camera"></i>
                <span class="no-select" slot="title">视频文件</span>
              </el-menu-item>
              <el-menu-item index="1-4" @click="goToPage('com')">
                <i class="el-icon-collection"></i>
                <span class="no-select" slot="title">压缩文件</span>
              </el-menu-item>
            </el-submenu>
            <el-menu-item index="2" @click="goToPage('sha')">
              <i class="el-icon-share"></i>
              <span class="no-select" slot="title"><strong>文件分享</strong></span>
            </el-menu-item>
            <el-menu-item index="3" @click="goToPage('rec')">
              <i class="el-icon-delete"></i>
              <span class="no-select" slot="title"><strong>回收站</strong></span>
            </el-menu-item>
            <el-menu-item index="4" @click="goToPage('log')">
              <i class="el-icon-switch-button"></i>
              <span class="no-select" slot="title"><strong>退出登录</strong></span>
            </el-menu-item>
          </el-menu>
        </transition>
        <!-- 内容页面，主页面获取其他页面的属性 -->
        <el-container class="main-container"><el-main>
            <router-view ref="MainView" @father="showAlertMessage"></router-view>
          </el-main></el-container>
      </el-container>
    </template>
    <!-- 登录界面 -->
    <template v-else>
      <router-view @father="showAlertMessage"></router-view>
    </template>
  </div>
</template>


<script>
export default {
  name: 'App',
  data() {
    return {
      isCollapsed: true, // 侧栏是否收缩
      defaultOpeneds: ['1'],

      // 提示框属性
      showAlert: false,
      alertType: '',
      alertTitle: '',
      alertDescription: ''
    };
  },
  computed: {
    isAuthPage() {
      return this.$route.name === 'log' || this.$route.name === 'reg'; // 判断是否为登录页面
    }
  },
  methods: {
    toggleCollapse() {
      this.isCollapsed = !this.isCollapsed;
    },
    goToPage(pageName) {
      if (this.$refs.MainView.isSearchActive) {
        this.$refs.MainView.isSearchActive = false;
      }
      this.$refs.MainView.searchQuery = '';
        const currentPath = '/';
        if (this.$route.name !== pageName || this.$route.query.path !== currentPath) {
          this.$router.push({ name: pageName, query: { path: '/' } });
        }
    },
    handleSelect(index) {
      if (index === '2' || index === '3') {
        this.defaultOpeneds = ['1'];//''为收缩菜单
      } else {
        this.defaultOpeneds = ['1'];//'1/'为展开菜单
      }
    },
    handleLogout() {
      localStorage.removeItem('token'); // 清除本地存储中的 Token
      this.$router.push({ name: 'log' }); // 跳转到登录页面
    },
    // 调用提示框
    showAlertMessage(type, title, description) {
      this.alertType = type;
      this.alertTitle = title;
      this.alertDescription = description;
      this.showAlert = true;
      // 隐藏alert提示框
      setTimeout(() => {
        this.showAlert = false;
      }, 3000); // 3秒后自动隐藏
    }
  },
};
</script>


<style>
    .el-menu-vertical-demo:not(.el-menu--collapse) {
      width: 200px;
      min-height: 400px;
    }
html,
body {
  height: 100%;
  margin: 0;
}
#app {
  height: 100vh;
  /* 设置应用的高度为视口高度的 100% */
  display: flex;
  /* 使用弹性盒布局 */
  flex-direction: column;
  /* 子元素垂直排列 */
  overflow: hidden;
  /* 隐藏溢出内容，禁止页面滚动 */
}

/* 标题样式 */
.el-header {
  display: flex;
  /* 使用弹性盒布局 */
  justify-content: center;
  /* 水平居中 */
  align-items: center;
  /* 垂直居中 */
  background-color: #409EFF;
  color: #ffffff;
  text-align: center;
  height: 60px;
}

/* 侧栏样式 */
.sidebar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  height: calc(100vh - 60px);
  background-color: #ffffff;
  color: #0073ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}
.no-select {
  -webkit-user-select: none;
  /* Chrome, Safari */
  -moz-user-select: none;
  /* Firefox */
  -ms-user-select: none;
  /* Internet Explorer/Edge */
  user-select: none;
  /* Non-prefixed version, currently supported by Chrome, Edge, Opera and Firefox */
}

/* 侧栏过渡效果 */
.sidebar-fade-enter-active,
.sidebar-fade-leave-active {
  transition: all 0.3s ease;
}

.sidebar-fade-enter,
.sidebar-fade-leave-to

/* .sidebar-fade-leave-active in <2.1.8 */
  {
  opacity: 0;
  transform: translateX(-100%);
}
/* 侧栏菜单 */

.logout-link {
  text-align: center;
  font-size: 14px;
  width: 100%;
}

/* 提示框 */
.floating-alert-view {
  position: fixed;
    top: 21%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 1000;
    width: 10%;}
.floating-alert {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 1000;
  width: 10%;
}
</style>
