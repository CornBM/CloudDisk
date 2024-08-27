import Vue from 'vue';
import VueRouter from 'vue-router';
import axios from 'axios';
// import SomeChildComponent from '@/components/SomeChildComponent'; //在 router-view 中传递 props
Vue.use(VueRouter);

const routes = [{
    path: '/all',
    name: 'all',
    component: () => import('../views/AllFileView.vue'),
    meta: {
      requiresAuth: true
    } // 需要认证的路由
  },
  {
    path: '/com',
    name: 'com',
    component: () => import('../views/AllFileView.vue'),
    meta: {
      requiresAuth: true
    } // 需要认证的路由
  },
  {
    path: '/pic',
    name: 'pic',
    component: () => import('../views/AllFileView.vue'),
    meta: {
      requiresAuth: true
    } // 需要认证的路由
  },
  {
    path: '/rec',
    name: 'rec',
    component: () => import('../views/RecycleView.vue'),
    meta: {
      requiresAuth: true
    } // 需要认证的路由
  },
  {
    path: '/sha',
    name: 'sha',
    component: () => import('../views/ShareView.vue'),
    meta: {
      requiresAuth: true
    } // 需要认证的路由
  },
  {
    path: '/vid',
    name: 'vid',
    component: () => import('../views/AllFileView.vue'),
    meta: {
      requiresAuth: true
    } // 需要认证的路由
  },
  {
    path: '/log',
    name: 'log',
    component: () => import('../views/pages/LoginView.vue'),
    // props: true // 确保 props 被传递
  },
  {
    path: '/reg',
    name: 'reg',
    component: () => import('../views/pages/RegisterView.vue')
  },
  {
    path: '/',
    redirect: '/log'
  }
];

const router = new VueRouter({
  routes
});

// 导航守卫
router.beforeEach((to, from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    const token = localStorage.getItem('token');
    if (!token) {
      this.$message({
        showClose: true,
        message: "请先登录",
        center: true,
        type: 'warning'
      });
      if (from.name !== 'log') {
        next({
              name: 'log'
        }); // 如果当前页面不是 log，则跳转到 log 页面
        // 未登录，跳转到登录页面
      }
    } else {
      // 发送请求到后端验证 token
      axios.get('/verify', {
          headers: {
            'Authorization': `${token}`
          }
        })
        .then(response => {
          if (response.data.code === '200') {
            next(); // Token 有效，允许访问
            }
            else {
              this.$message({
                showClose: true,
                message: 'Token无效，请重新登录',
                center: true,
                type: 'error'
              });
              next({
                name: 'log'
              }); // 如果当前页面不是 log，则跳转到 log 页面
            }
            })
            .catch(error => {
                  console.error('Token 验证失败:', error);
                  this.$message({
                    showClose: true,
                    message: 'Token验证失败，请重新登录',
                    center: true,
                    type: 'error'
                  });
                  next({
            name: 'log'
          }); // 如果当前页面不是 log，则跳转到 log 页面
        });
    }
  } else {
    next(); // 不需要认证的路由，允许访问
  }
});

export default router;
