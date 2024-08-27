<template>
    <div class="background-register">
        <div class="box-center-father-2">
            <div class="box-center-son-2">
                <el-card>
                    <div>
                        <div class="head-text-2">欢迎注册个人云盘系统</div>
                        <el-divider></el-divider>
                        <div class="center-image-2">
                            <img src="../../assets/sign.png" width="150px">
                        </div>
                        <div class="register-form">
                            <input class="register-input" type="text" placeholder="账号" v-model="account_name">
                            <div class="password-container-2">
                                <input class="register-input" :type="passwordFieldType" v-model="password"
                                    placeholder="密码">
                                <span class="show-password-2" @click="togglePasswordVisibility">{{ passwordFieldType ===
                                    'password' ? '显示' : '隐藏' }}</span>
                            </div>
                            <br>
                            <div>
                                <button class="register-button" @click="onRegister">注册</button>
                            </div>
                        </div>
                        <div class="login-button-2">
                            <el-button type="text" @click="onRegister">
                                <router-link to="/log">前往登录</router-link>
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
        };
    },
    methods: {
        togglePasswordVisibility() {
            this.passwordFieldType = this.passwordFieldType === 'password' ? 'text' : 'password';
        },
        onRegister: function () {
            if (this.account_name === 'all') {
                this.$message({
                    showClose: true,
                    message: '注册失败：账号不能为all！',
                    center: true,
                    type: 'warning'
                });
            } else if (this.account_name === '') {
                this.$message({
                    showClose: true,
                    message: '注册失败：账号不能为空！',
                    center: true,
                    type: 'warning'
                });
            }
            else {
                const payload = {
                    account_name: this.account_name,
                    password: this.password,
                };
                this.$axios.post('/sign', payload)
                    .then((response) => {
                        const responseData = response.data;
                        if (responseData.code === "200") {
                            this.$message({
                                showClose: true,
                                message: '注册成功！' + responseData.msg,
                                center: true,
                                type: 'success'
                            });
                            // 显示成功信息
                            this.$router.push({ name: 'log' }); // 跳转到登录页面
                        } else {
                            this.$message({
                                showClose: true,
                                message: '注册失败:' + responseData.msg,
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
    },
    mounted() {
    },
};
</script>


<style>
.background-login {
    background-color: aliceblue;
}

.box-center-father-2 {
    position: relative;
}

.box-center-son-2 {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, 25%);
    width: 500px;
    height: 500px;
}

.head-text-2 {
    text-align: center;
    font-weight: bold;
    color: rgb(0, 0, 0);
    font-size: 25px;

}

.center-image-2 {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100px;
    /* 父元素高度 */
}

.register-form {
    display: flex;
    flex-direction: column;
    align-items: center;
    background-color: white;
    /* 白色背景 */
    padding: 20px;
    border-radius: 12px;
    /* 圆角 */
}

.register-input {
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

.password-container-2 {
    position: relative;
    width: 450px;
    text-align: center;
}

.show-password-2 {
    position: absolute;
    right: 50px;
    top: 50%;
    transform: translateY(-50%);
    cursor: pointer;

}

.register-button {
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

.register-button:hover {
    background-color: #0056b3;
    /* 悬停时的颜色 */
}

.login-button-2 {
    text-align: right;
    background-color: white;
}
</style>