<template>
    <div class="share-view">
        <el-row>
            <el-col :span="24">

                <el-autocomplete v-model="user_name" :fetch-suggestions="querySearchAsync" placeholder="请输入用户名"
                    @select="handleSelect"></el-autocomplete>
                &nbsp;
                <el-input class="input-with-select-2" placeholder="请输入文件名" v-model="searchQuery"></el-input>

                <el-button type="primary" icon="el-icon-search" @click="searchShare">搜索</el-button>
                <el-button type="danger" icon="el-icon-delete" @click="searchQuery = ''; user_name = '';">
                    清空
                </el-button>
            </el-col>
        </el-row>

        <div>
            <el-table :data="paginatedFiles" style="width: 100%" @selection-change="handleSelectionChange"
                height="calc(100vh - 180px)" @sort-change="handleSortChange" @row-dblclick="handleRowDblClick">
                <el-table-column width="150px" prop="user_name" label="分享者" sortable="custom"></el-table-column>
                <el-table-column width="200px" prop="name" label="文件名" sortable="custom">
                    <template slot-scope="scope">{{ (scope.row.type === 'folder' ? '📁' : '📄') + scope.row.name
                        }}</template>
                </el-table-column>
                <el-table-column width="150px" prop="size" label="大小" sortable="custom"></el-table-column>
                <el-table-column prop="modified" label="修改时间" sortable="custom"></el-table-column>
                <el-table-column prop="created" label="创建时间" sortable="custom"></el-table-column>
                <el-table-column label="操作" width="250px">
                    <template slot-scope="scope">
                        <el-button @click="downloadFile(scope.row)" icon='el-icon-download' type="primary"
                            size="mini">下载</el-button>
                        <el-button @click="cancelShare(scope.row)" icon="el-icon-circle-close" type="danger"
                            size="mini">
                            取消分享
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <div>
            <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                :current-page="currentPage" :page-sizes="[10, 20, 50, 100, 200]" :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper" :total="total">
            </el-pagination>
        </div>

    </div>
</template>

<script>
import axios from 'axios';
// import { MessageBox } from 'element-ui';

export default {
    data() {
        return {
            users: [],
            user_name: '',
            timeout: null,

            files: [],
            searchQuery: '',
            currentPage: 1,
            pageSize: 10,
            total: 4, // 文件总数
            sortOrder: null,
            sortBy: null
        };
    },
    computed: {
        sortedFiles() {
            let sorted = [...this.files];
            if (this.sortBy) {
                sorted.sort((a, b) => {
                    if (this.sortOrder === 'ascending') {
                        return a[this.sortBy] > b[this.sortBy] ? 1 : -1;
                    } else if (this.sortOrder === 'descending') {
                        return a[this.sortBy] < b[this.sortBy] ? 1 : -1;
                    }
                    return 0;
                });
            }
            return sorted;
        },
        paginatedFiles() {
            const startIndex = (this.currentPage - 1) * this.pageSize;
            return this.sortedFiles.slice(startIndex, startIndex + this.pageSize);
        },
    },
    methods: {
        searchShare() {
            const data = {
                user_name: this.user_name,
                name: this.searchQuery
            };
            axios.post('/searchShare', data, {
                headers: { 'Authorization': `${localStorage.getItem('token')}` }
            })
                .then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        // 处理返回的文件数据
                        this.files = responseData.data;
                        this.total = responseData.data.length;
                    } else {
                        this.$message({
                            showClose: true,
                            message: '搜索失败：' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                        this.files = [];
                    }
                })
                .catch(error => {
                    this.$message({
                        showClose: true,
                        message: '搜索错误：' + error,
                        center: true,
                        type: 'error'
                    });
                });
        },
        cancelShare(file) {
            console.log('分享文件:', file);
            var fileNames = [file.name];
            const sharePath = file.path;

            axios.post('/unshare', {
                name: sharePath,
                path: fileNames
            }, {
                headers: { 'Authorization': `${localStorage.getItem('token')}` }
            })
                .then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: file.name + '取消分享成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.searchShare();
                    } else {
                        this.$message({
                            showClose: true,
                            message: '取消分享失败: ' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                        this.searchShare();
                    }
                })
                .catch(error => {
                    this.$message({
                        showClose: true,
                        message: '取消分享错误：' + error,
                        center: true,
                        type: 'error'
                    });
                    this.searchShare();
                });
        },
        downloadFile(file) {
            console.log('下载文件/打开文件夹:', file);

            // 创建请求数据
            const requestData = {
                path: file.path + '/' + file.name,
                user_name: file.user_name,
            };

            // 发送POST请求获取文件的blob数据流
            axios.post('/downloadShare', requestData, {
                responseType: 'blob', // 指定响应类型为blob
                headers: {
                    'Authorization': `${localStorage.getItem('token')}`
                }
            }).then(response => {
                // 创建一个URL对象指向blob数据
                const blobUrl = window.URL.createObjectURL(new Blob([response.data]));

                // 创建一个隐藏的链接元素
                const link = document.createElement('a');
                link.href = blobUrl;

                // 设置下载文件名
                link.setAttribute('download', file.name);

                // 将链接元素添加到页面
                document.body.appendChild(link);

                // 模拟点击下载
                link.click();

                // 移除链接元素
                document.body.removeChild(link);

                // 释放blob URL
                window.URL.revokeObjectURL(blobUrl);
            }).catch(error => {
                this.$message({
                    showClose: true,
                    message: '文件下载错误：' + error,
                    center: true,
                    type: 'error'
                });
            });
        },
        handleRowDblClick(row) {
            this.downloadFile(row);
        },
        handleSelectionChange(val) {
            this.selectedFiles = val;
        },
        handleSortChange({ prop, order }) {
            this.sortBy = prop;
            this.sortOrder = order;
        },
        handleSizeChange(size) {
            this.pageSize = size;
            console.log('每页显示条目:', size);
        },
        handleCurrentChange(page) {
            this.currentPage = page;
            console.log('当前页:', page);
        },
        //根据输入内容提供对应的输入建议
        loadAll() {

            axios.post('/userlist', {}, {
                headers: { 'Authorization': `${localStorage.getItem('token')}` }
            })
                .then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        const transformedData = responseData.data.map(user => ({
                            value: user.account_name
                        }));
                        console.log('加载用户成功！', transformedData);
                        // 返回转换后的数据
                        this.users = transformedData;
                    } else {
                        console.error('加载用户失败:', responseData.msg);
                        this.users = [{ "value": "all" }];
                    }
                })
                .catch(error => {
                    console.error('加载用户失败:', error);
                });
        },
        querySearchAsync(queryString, cb) {
            var users = this.users;
            var results = queryString ? users.filter(this.createStateFilter(queryString)) : users;
            cb(results);
        },
        createStateFilter(queryString) {
            return (users) => {
                return (users.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
            };
        },
        handleSelect(item) {
            console.log(item);
        }
    },

    watch: {
        // 监听路由变化，重新加载文件列表
        $route() {
            this.currentPage = 1; // 重置当前页
            if (this.$route.name === "sha")
                this.searchShare();
        },
    }, mounted() {
        this.searchShare(); // 初始加载文件
        this.loadAll(); // 加载输入建议数据
    }
};
</script>

<style scoped>
/* 分享页面 */
.share-view {
    padding: 20px;
}
/* 搜索框 */
.input-with-select-2 {
    width: 300px;
    margin-right: 10px;
}
</style>
