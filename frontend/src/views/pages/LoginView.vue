<template>
    <div class="background-login">
        <div class="box-center-father">
            <div class="box-center-son">
                <el-card>
                    <div>
                        <div class="head-text">欢迎登录个人云盘系统</div>
                        <el-divider></el-divider>
                        <div class="center-image">
                            <img src="../../assets/login.png" width="150px">
                        </div>
                        <div class="login-form">
                            <input class="login-input" type="text" placeholder="账号" v-model="account_name">
                            <div class="password-container">
                                <input class="login-input" :type="passwordFieldType" v-model="password"
                                    placeholder="密码">
                                <span class="show-password" @click="togglePasswordVisibility">{{ passwordFieldType ===
                                    'password' ? '显示' : '隐藏' }}</span>
                            </div>
                            <br>
                            <div>
                                <el-select v-model="value" placeholder="请选择">
                                    <el-option v-for="item in options" :key="item.value" :label="item.label"
                                        :value="item.value"></el-option>
                                </el-select>
                                &nbsp;
                                <button class="login-button" @click="onLogin">登录</button>
                            </div>
                        </div>
                        <div class="register-button-2">
                            <el-button type="text">
                                <router-link to="/reg">前往注册 </router-link>
                            </el-button>
                        </div>
                    </div>
                </el-card>
            </div>
        </div>
    </div>
</template>


<script>
export default {
    data() {
        return {
            account_name: '',
            password: '',
            passwordFieldType: 'password',
            options: [
                { value: 'admin', label: '管理员' },
                { value: 'user', label: '普通用户' }
            ],
            value: 'user',
        };
    },
    methods: {
        togglePasswordVisibility() {
            this.passwordFieldType = this.passwordFieldType === 'password' ? 'text' : 'password';
        },
        onLogin() {
            if (this.account_name === 'all') {
                // this.$emit("father", "error","登录失败","账号不能为all");
                this.$message({
                    showClose: true,
                    message: '登录失败：账号不能为all!',
                    center: true,
                    type: 'warning'
                });
            } else if (this.account_name === '') {
                this.$message({
                    showClose: true,
                    message: '登录失败：账号不能为空',
                    center: true,
                    type: 'warning'
                });
            }
            else {
                const payload = {
                    account_name: this.account_name,
                    password: this.password,
                    identity: this.value
                };
                this.$axios.post('/login', payload)
                    .then((response) => {
                        const responseData = response.data;
                        if (responseData.code === "200") {
                            this.$message({
                                showClose: true,
                                message: "登录成功：" + responseData.msg,
                                center: true,
                                type: 'success'
                            });
                            // 保存Token到localStorage
                            localStorage.setItem('token', responseData.data);
                            // alert(JSON.stringify(responseData));
                            this.$router.push({ name: 'all' });
                        } else {
                            this.$message({
                                showClose: true,
                                message: "登录失败：" + responseData.msg,
                                center: true,
                                type: 'error'
                            });
                        }
                    })
                    .catch((error) => {
                        this.$message({
                            showClose: true,
                            message: '请求错误:' + error,
                            center: true,
                            type: 'error'
                        });
                    });
            }
        },
    }
}
</script>


<style>
.background-login {
    background-color: aliceblue;
}

/* 登录卡片居中：父子元素 */
.box-center-father {
    position: relative;
}

.box-center-son {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, 25%);
    width: 500px;
    height: 500px;
}

/* 标题 */
.head-text {
    text-align: center;
    font-weight: bold;
    color: rgb(0, 0, 0);
    font-size: 25px;
}

/* logo */
.center-image {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100px;
    /* 父元素高度 */
}

/* 输入框和按钮 */
.login-form {
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: white;
    /* 白色背景 */
    padding: 20px;
    border-radius: 12px;
    /* 圆角 */
}

/* 账号和密码输入框和登录按钮容器 */
.login-input {
    width: 350px;
    /* 输入框宽度 */
    padding: 10px;
    margin: 10px 0;
    border: 2px solid #b8ddff;
    /* 蓝色边框 */
    border-radius: 6px;
    /* 圆角 */
    font-size: 20px;
    /* 字体大小 */
    text-align: center;
}

/* 密码输入框 */
.password-container {
    position: relative;
    width: 450px;
    text-align: center;
}

/* 密码输入框右侧显示/隐藏密码按钮 */
.show-password {
    position: absolute;
    right: 50px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;
}

/* 登录按钮 */
.login-button {
    background-color: #409EFF;
    /* 蓝色 */
    color: white;
    /* 白色文字 */
    border: none;
    /* 去除边框 */
    border-radius: 6px;
    /* 圆角 */
    padding: 10px 40px;
    /* 增加内边距以增加按钮长度 */
    font-size: 16px;
    /* 字体大小 */
    cursor: pointer;
    /* 鼠标指针样式 */
    transition: background-color 0.3s;
    /* 背景颜色过渡效果 */
    margin-top: 20px;
    /* 按钮顶部外边距 */
}

/* 登录按钮悬停样式 */
.login-button:hover {
    background-color: #0068d6;
    /* 悬停时的颜色 */
}

/* 注册按钮 */
.register-button-2 {
    text-align: right;
    background-color: white;
}
</style>
