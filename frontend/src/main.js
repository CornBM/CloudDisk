import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from 'axios';

// 设置axios默认基础URL
// axios.defaults.baseURL = 'http://api.doc.jiyou-tech.com/mock/28343';
axios.defaults.baseURL = window.config.apiBaseUrl;
// 将axios挂载到Vue原型上，这样可以在组件中直接使用this.$axios
Vue.prototype.$axios = axios;


Vue.use(ElementUI);
Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
