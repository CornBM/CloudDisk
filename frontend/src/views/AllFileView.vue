<template>
    <div class="all-file-view" :style="{ transform: `1.3`, transformOrigin: 'top left' }">

        <el-row><el-col :span="24" class="controls">
                <el-input class="input-with-select" placeholder="请输入文件名" v-model="searchQuery"></el-input>
                <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
                <el-button type="primary" icon="el-icon-upload" @click="uploadFile">上传</el-button>
                <el-button type="primary" icon="el-icon-folder-add" @click="createFolder">新建</el-button>
                <el-button type="danger" icon="el-icon-delete" @click="batchDelete">删除</el-button>
                <el-input class="path-input" placeholder="当前路径" v-model="currentPath"
                    :disabled="isSearchActive"></el-input>
                <el-button type="primary" icon="el-icon-arrow-right" @click="enterPath"
                    :disabled="isSearchActive">进入</el-button>
                <el-button type="primary" icon="el-icon-arrow-left" @click="goBack">返回</el-button>
            </el-col>
        </el-row>

        <br><el-progress v-if="uploading" :percentage="uploadProgress"></el-progress>

        <!-- <el-slider v-model="scale" :min="0.5" :max="2" step="0.1"></el-slider> -->

        <div class="table-container" @contextmenu.prevent @contextmenu="handleAllFileViewContextMenu">
            <el-table v-if="!isThumbnailView" ref="table" :data="paginatedFiles" style="width: 100%"
                height="calc(100vh - 180px)" @selection-change="handleSelectionChange" @sort-change="handleSortChange"
                @row-contextmenu="handleRowContextMenu" @row-dblclick="handleRowDblClick">
                <el-table-column type="selection"></el-table-column>
                <el-table-column prop="name" label="文件名" sortable="custom" width="200px">
                    <template slot-scope="scope">{{ (scope.row.type === 'folder' ? '🗂️' : fileEmoji(scope.row.name)) +
                        scope.row.name }}</template>
                </el-table-column>
                <el-table-column prop="size" label="大小" sortable="custom" width="100px">
                    <template slot-scope="scope">{{ (scope.row.type === 'folder' ? '' : scope.row.size) }}</template>
                </el-table-column>
                <el-table-column prop="modified" label="修改时间" sortable="custom"></el-table-column>
                <el-table-column prop="created" label="创建时间" sortable="custom"></el-table-column>
                <el-table-column label="操作" width="400px">
                    <template slot-scope="scope">
                        <el-button @click="downloadFile(scope.row)"
                            :icon="scope.row.type === 'folder' ? 'el-icon-folder-opened' : 'el-icon-download'"
                            type="primary" size="mini">{{ scope.row.type === 'folder' ? '打开' : '下载' }}</el-button>
                        <el-button @click="editFile(scope.row)" type="info" size="mini"
                            icon="el-icon-refresh-right">重命名</el-button>
                        <el-button @click="deleteFile(scope.row)" type="danger" size="mini"
                            icon="el-icon-delete">删除</el-button>
                        <el-button icon="el-icon-share" @click="shareFile(scope.row)" type="success"
                            size="mini">分享</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <template>
                <div class="thumbnail-all-view" v-if="isThumbnailView">
                    <div class="thumbnail-view">
                        <span v-for="file in paginatedFilesWithThumbnail" :key="file.id">
                            <template v-if="!file.loaded">
                                <el-skeleton :rows="1" animated>
                                    <template #template>
                                        <div class="thumbnail">
                                            <el-skeleton-item variant="image" class="thumbnail-img" />
                                            <el-skeleton-item variant="text" class="file-name" />
                                        </div>
                                    </template>
                                </el-skeleton>
                            </template>
                            <template>
                                <div v-show="file.loaded" class="thumbnail">
                                    <img @load="file.loaded = true" :src="file.url" alt="缩略图" class="thumbnail-img"
                                        ref="thumbnailImages" @dblclick="downloadFile(file)" />
                                    <div class="file-name" @dblclick="copyFileName(file.name)" :title="file.name">{{
                                        file.name }}</div>
                                </div>
                            </template>
                        </span>
                    </div>
                </div>
            </template>

            <div class="pagination-container">
                <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange"
                    :current-page="currentPage" :page-sizes="[7, 21, 70, 140, 700]" :page-size="pageSize"
                    layout="total, sizes, prev, pager, next, jumper" :total="total">
                </el-pagination>
            </div>

            <div class="context-menu" id="context-menu">
                <ul>
                    <li><el-button plain icon="el-icon-scissors" class="no-radius" @click="cut">剪切</el-button></li>
                    <li><el-button plain icon="el-icon-copy-document" class="no-radius" @click="copy">复制</el-button>
                    </li>
                    <li><el-button plain icon="el-icon-document-add" class="no-radius" :disabled="!selectedAction"
                            @click="paste">粘贴</el-button></li>
                </ul>
            </div>

        </div>
    </div>
</template>


<script>
import axios from 'axios';
import { MessageBox } from 'element-ui';

export default {
    data() {
        return {
            scale: 1, // 初始缩放比例为 1

            searchQuery: '',
            files: [],
            selectedFiles: [],
            currentPage: 1,
            pageSize: 21,
            total: 0, // 总文件数
            sortOrder: null,
            sortBy: null,
            currentPath: '/',
            isThumbnailView: true,
            previousPath: '/', // 添加 previousPath 用于保存上一次正确的路径

            uploading: false, // 添加上传状态
            uploadProgress: 0, // 添加上传进度

            isSearchActive: false, // 用于指示搜索操作是否激活

            selectedAction: null, // 用于保存选中的操作
            selectedPaths: [], // 用于保存选中的文件/文件夹路径
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
        paginatedFilesWithThumbnail() {
            const startIndex = (this.currentPage - 1) * this.pageSize;
            return this.files.slice(startIndex, startIndex + this.pageSize);
        },
        fileEmoji() {// 根据文件名获取对应的 emoji
            return function (filename) {
                const extension = filename.toLowerCase().split('.').pop();
                switch (extension) {
                    case 'doc': case 'docx':
                        return '📘';
                    case 'pdf':
                        return '📕';
                    case 'txt':
                        return '📝';
                    case 'jpg': case 'jpeg': case 'png': case 'bmp': case 'gif':
                        return '🖼️';
                    case 'mp3': case 'wav': case 'wma': case 'aac':
                        return '🎵';
                    case 'mp4': case 'avi': case 'wmv': case 'flv':
                        return '🎬';
                    case 'xlsx': case 'xls':
                        return '📗';
                    case 'ppt': case 'pptx':
                        return '📙';
                    case 'zip': case 'rar': case 'tar': case '7z':
                        return '📚';
                    default:
                        return '📄';
                }
            }
        }
    },
    methods: {
        fetchFiles() {
            const category = this.$route.name;
            const path = this.currentPath;

            axios.post('/file', { category, path }, {
                headers: { 'Authorization': `${localStorage.getItem('token')}` }
            })
                .then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        console.log('跳转成功！', responseData.msg);
                        // 保存当前路径为上一次正确的路径
                        this.previousPath = this.currentPath;
                        this.files = response.data.data;
                        this.total = response.data.data.length;
                        this.currentPath = path;
                        this.data = response.data;
                        // 更新地址栏中的路径
                        this.updateBrowserPath();
                    } else {
                        // 显示错误信息
                        this.$message({
                            showClose: true,
                            message: "目录不存在:" + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                        this.currentPath = this.previousPath; // 恢复到上一次正确的路径
                        this.$router.replace({ name: this.$route.name, query: { path: this.previousPath } });
                    }
                })
                .catch(error => {
                    console.error('获取文件列表失败:', error);
                    this.currentPath = this.previousPath; // 恢复到上一次正确的路径
                });
            this.isThumbnailView = category === 'pic';
            // this.isThumbnailView = category === 'pic' || category === 'vid';

        },
        search() {
            console.log('搜索:', this.searchQuery);
            const category = this.$route.name;
            axios.post('/search', {
                name: this.searchQuery,
                category: category,
                path: this.currentPath,
            }, {
                headers: { 'Authorization': `${localStorage.getItem('token')}` }
            })
                .then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {

                        this.files = response.data.data.map(file => {
                            return {
                                ...file,
                                url: "", // 生成url并添加到file对象
                                loaded: false // 初始化加载状态为false
                            };
                        });
                        // alert('files changed');
                        this.total = response.data.data.length;
                        this.isSearchActive = true;
                        this.isThumbnailView = category === 'pic';
                        // this.isThumbnailView = category === 'pic' || category === 'vid';
                        this.loadImg(); // 加载图片

                    } else {
                        this.$message({
                            showClose: true,
                            message: '搜索失败：' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                    }
                })
                .catch(error => {
                    this.$message({
                        showClose: true,
                        message: "搜索失败" + error,
                        center: true,
                        type: 'error'
                    });
                    this.isSearchActive = true;
                });

        },
        uploadFile() {
            console.log('上传文件');

            // 创建一个隐藏的文件输入元素
            const input = document.createElement('input');
            input.type = 'file';
            input.multiple = true; // 允许选择多个文件

            // 监听文件选择
            input.onchange = (event) => {
                const files = event.target.files;
                if (!files.length) {
                    return;
                }

                // 构建表单数据
                const formData = new FormData();
                for (const file of files) {
                    formData.append('file', file);
                }

                // 构建URL参数
                const uploadPath = this.currentPath;
                const url = `/upload?path=${encodeURIComponent(uploadPath)}`;

                // 发送POST请求上传文件
                this.uploading = true; // 设置上传状态为true
                axios.post(url, formData, {
                    headers: {
                        'Authorization': `${localStorage.getItem('token')}`,
                        'Content-Type': 'multipart/form-data'
                    },
                    onUploadProgress: (progressEvent) => {
                        this.uploadProgress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    }
                }).then(response => {
                    const responseData = response.data;
                    this.uploading = false; // 设置上传状态为false
                    this.uploadProgress = 0; // 重置上传进度
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: '文件上传成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles(); // 刷新文件列表
                    } else {
                        this.$message({
                            showClose: true,
                            message: '上传失败:' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                    }
                }).catch(error => {
                    this.uploading = false; // 设置上传状态为false
                    this.uploadProgress = 0; // 重置上传进度
                    this.$message({
                        showClose: true,
                        message: '文件上传失败：' + error,
                        center: true,
                        type: 'error'
                    });
                });
            };

            // 模拟点击文件输入
            input.click();
        },
        createFolder() {
            console.log('新建文件夹');

            this.$prompt('请输入新文件夹的名称', '新建文件夹', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                inputValue: '新建文件夹'
            }).then(({ value }) => {
                if (!value.trim()) {
                    this.$message({
                        showClose: true,
                        message: '文件夹名称不能为空',
                        center: true,
                        type: 'warning'
                    });
                    return;
                }

                const folderPath = this.currentPath;
                const data = {
                    path: folderPath,
                    name: value
                };

                axios.post('/mkdir', data, {
                    headers: {
                        'Authorization': `${localStorage.getItem('token')}`
                    }
                }).then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: '文件夹创建成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles(); // 刷新文件列表
                    } else {
                        this.$message({
                            showClose: true,
                            message: '文件夹创建失败：' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                    }
                }).catch(error => {
                    this.$message({
                        showClose: true,
                        message: '文件夹创建错误：' + error,
                        center: true,
                        type: 'error'
                    });
                });
            }).catch(() => {
                console.log('取消创建文件夹');
            });
        },
        batchDelete() {
            console.log('批量删除:', this.selectedFiles);

            // 检查是否有选中的文件/文件夹
            if (this.selectedFiles.length === 0) {
                this.$message({
                    showClose: true,
                    message: '请选择要删除的文件/文件夹',
                    center: true,
                    type: 'warning'
                });
                return;
            }

            // 使用Element UI的MessageBox确认删除操作
            MessageBox.confirm('确定要删除选中的文件/文件夹吗？', '确认删除', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 构建删除路径列表
                const paths = this.selectedFiles.map(file => file.path + file.name);

                // 构建请求数据
                const requestData = { path: paths };

                // 发送POST请求进行批量删除
                axios.post('/delete', requestData, {
                    headers: {
                        'Authorization': `${localStorage.getItem('token')}`
                    }
                }).then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: '删除成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles(); // 刷新文件列表
                    } else {
                        this.$message({
                            showClose: true,
                            message: '删除失败：' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                    }
                }).catch(error => {
                    this.$message({
                        showClose: true,
                        message: '删除失败：' + error,
                        center: true,
                        type: 'error'
                    });
                });
            }).catch(() => {
                console.log('取消删除操作');
            });
        },
        deleteFile(file) {
            console.log('删除文件:', file);

            // 使用Element UI的MessageBox确认删除操作
            MessageBox.confirm('确定要删除该文件/文件夹吗？', '确认删除', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                // 构建文件路径
                const filePath = file.path +'/' + file.name;

                // 构建请求数据
                const requestData = { path: [filePath] };
                // alert(JSON.stringify(requestData));

                // 发送POST请求删除文件
                axios.post('/delete', requestData, {
                    headers: {
                        'Authorization': `${localStorage.getItem('token')}`
                    }
                }).then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: '删除成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles(); // 刷新文件列表
                    } else {
                        this.$message({
                            showClose: true,
                            message: '删除失败：' + responseData.ms,
                            center: true,
                            type: 'error'
                        });
                    }
                }).catch(error => {
                    this.$message({
                        showClose: true,
                        message: '删除错误：' + error,
                        center: true,
                        type: 'error'
                    });
                });
            }).catch(() => {
                console.log('取消删除操作');
            });
        },
        enterPath() {
            console.log('进入路径:', this.currentPath);

            // 正则表达式，匹配路径中包含相对路径符号
            const relativePathRegex = /(?:^|[\\/])\.\.(?:[\\/]|$)/;

            // 检查当前路径是否包含相对路径符号
            if (relativePathRegex.test(this.currentPath)) {
                this.$message({
                    showClose: true,
                    message: '不允许访问相对路径',
                    center: true,
                    type: 'warning'
                });
                this.currentPath = this.previousPath; // 恢复到上一次正确的路径
                return;
            }

            // 如果当前路径与路由中的路径不一致，则进行路由跳转
            if (this.currentPath !== this.$route.query.path) {
                this.$router.push({ name: this.$route.name, query: { path: this.currentPath } })
                    .catch(err => {
                        if (err.name !== 'NavigationDuplicated') {
                            throw err;
                        }
                    });
            }
        },
        goBack() {
            if (this.isSearchActive) {
                if (this.$route.name === 'all') {
                    this.fetchFiles();
                    this.isSearchActive = false;
                } else {
                    this.searchQuery = '';
                    this.search();
                    // alert(this.isSearchActive);
                }
                return;
            }
            if (this.currentPath === '/') {
                return;
            }
            const paths = this.currentPath.split('/');
            paths.pop();
            this.currentPath = paths.join('/') || '/';
            this.fetchFiles();
        },
        updateBrowserPath() {
            // 更新地址栏中的路径
            if (this.currentPath !== this.$route.query.path) {
                this.$router.replace({ name: this.$route.name, query: { path: this.currentPath } });
            }
        },
        downloadFile(file) {
            console.log('下载文件/打开文件夹:', file);
            // 执行下载操作
            if (file.type === 'folder') {
                if (this.isSearchActive) {
                    this.isSearchActive = false;
                }
                if (file.path === '/') {
                    this.currentPath = file.path + file.name;
                } else {
                    this.currentPath = file.path + '/' + file.name;
                }
                this.fetchFiles();
            } else {

                console.log('下载文件:', file);

                // 创建请求数据
                const requestData = {
                    path: file.path + '/' + file.name
                };

                // 发送POST请求获取文件的blob数据流
                axios.post('/download', requestData, {
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
                        message: '文件下载错误：', error,
                        center: true,
                        type: 'error'
                    });
                });

            }
        },
        editFile(file) {
            console.log('重命名文件:', file);
            // alert(JSON.stringify(file));

            this.$prompt('请输入新的文件名', '重命名文件', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                inputValue: file.name // 设置输入框的默认值为文件的当前名称
            }).then(({ value }) => {
                const data = {
                    path: (file.path=== '/'? "" : file.path) + "/" + file.name, // 使用file.path + file.name代替当前路径和文件名
                    name: value
                };

                axios.post('/rename', data, {
                    headers: {
                        'Authorization': `${localStorage.getItem('token')}`
                    }
                }).then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: '文件重命名成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles(); // 刷新文件列表
                    } else {
                        this.$message({
                            showClose: true,
                            message: '文件重命名失败：' + responseData.msg,
                            center: true,
                            type: 'error'
                        });

                    }
                }).catch(error => {
                    this.$message({
                        showClose: true,
                        message: '文件重命名错误：' + error,
                        center: true,
                        type: 'error'
                    });
                });
            }).catch(() => {
                console.log('取消重命名文件');
            });
        },
        shareFile(file) {
            console.log('分享文件:', file);
            var fileNames = [];
            if (this.selectedFiles.length === 0) {
                fileNames = [file.name];
            } else {
                fileNames = this.selectedFiles.map(file => file.name);
            }
            const sharePath = file.path;

            axios.post('/share', {
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
                            message: file.name + '分享成功：' + responseData.msg,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles(); // 刷新文件列表
                    } else {
                        this.$message({
                            showClose: true,
                            message: '文件分享失败: ' + responseData.msg,
                            center: true,
                            type: 'error'
                        });
                    }
                })
                .catch(error => {
                    this.$message({
                        showClose: true,
                        message: '文件分享失败：' + error,
                        center: true,
                        type: 'error'
                    });
                });
        },
        handleRowDblClick(row) {
            this.downloadFile(row);
        },
        handleRowContextMenu(row, column, event) {
            event.preventDefault();
            const contextMenu = document.getElementById('context-menu');
            const isSelected = this.$refs.table.selection.some(selectedRow => selectedRow === row);

            // 如果当前行未被选中，则清除当前选中状态并选中当前行
            if (!isSelected) {
                this.$refs.table.clearSelection();
                this.$refs.table.toggleRowSelection(row, true);
            }
            this.$refs.table.toggleRowSelection(row, true);
            contextMenu.style.display = 'block';
            contextMenu.style.left = `${event.pageX}px`;
            contextMenu.style.top = `${event.pageY}px`;
        },
        handleAllFileViewContextMenu(event) {
            event.preventDefault();
            const contextMenu = document.getElementById('context-menu');
            contextMenu.style.display = 'block';
            contextMenu.style.left = `${event.pageX}px`;
            contextMenu.style.top = `${event.pageY}px`;
        },
        handleSizeChange(size) {
            this.pageSize = size;
            console.log('每页显示条目:', size);
        },
        handleCurrentChange(page) {
            this.currentPage = page;
            console.log('当前页:', page);
        },
        handleSelectionChange(val) {
            this.selectedFiles = val;
        },
        handleSortChange({ prop, order }) {
            this.sortBy = prop;
            this.sortOrder = order;
        },
        cut() {
            this.selectedAction = 'cut';
            this.selectedPaths = this.selectedFiles.length > 0
                ? this.selectedFiles.map(file => (file.path === '/' ? "" : file.path) + '/' + file.name)
                : this.selectedPaths;
        },
        copy() {
            this.selectedAction = 'copy';
            this.selectedPaths = this.selectedFiles.length > 0
                ? this.selectedFiles.map(file => (file.path === '/' ? "" : file.path) + '/' + file.name)
                : this.selectedPaths;
        },
        paste() {
            if (this.selectedAction && this.selectedPaths.length > 0) {
                const url = this.selectedAction === 'cut' ? '/move' : '/copy';
                const requestData = { path: [...this.selectedPaths, this.currentPath] };

                axios.post(url, requestData, {
                    headers: {
                        'Authorization': `${localStorage.getItem('token')}`
                    }
                }).then(response => {
                    const responseData = response.data;
                    if (responseData.code === "200") {
                        this.$message({
                            showClose: true,
                            message: `${this.selectedAction === 'cut' ? '剪切' : '复制'}成功`,
                            center: true,
                            type: 'success'
                        });
                        this.fetchFiles();
                    } else {
                        this.$message({
                            showClose: true,
                            message: `${this.selectedAction === 'cut' ? '剪切' : '复制'}失败`,
                            center: true,
                            type: 'error'
                        });
                    }
                    this.selectedAction = null;
                    this.contextMenuVisible = false;
                }).catch(error => {
                    this.$message({
                        showClose: true,
                        message: `${this.selectedAction === 'cut' ? '剪切' : '复制'}错误` + error,
                        center: true,
                        type: 'error'
                    });
                    this.selectedAction = null;
                    this.contextMenuVisible = false;
                });
            }
        },
        loadImg() {
            if (this.isThumbnailView) {
                const files = this.paginatedFilesWithThumbnail;
                for (const file of files) {
                    const path = (file.path === '/' ? '' : file.path) + '/' + file.name;
                    console.log(path);
                    const requestData = {
                        path: path,
                        category: this.$route.name,
                    };
                    // 发送POST请求获取文件的blob数据流
                    axios.post('/download', requestData, {
                        responseType: 'blob', // 指定响应类型为blob
                        headers: {
                            'Authorization': `${localStorage.getItem('token')}`
                        }
                    }).then(response => {
                        // 创建一个URL对象指向blob数据
                        const blobUrl = window.URL.createObjectURL(new Blob([response.data]));
                        file.url = blobUrl;
                    }).catch(error => {
                        console.error('图片下载失败，请稍后重试。', error);
                    });
                }
            }
        },
        copyFileName(name) {
            const textarea = document.createElement('textarea');
            textarea.value = name;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            this.$message({
                showClose: true,
                message: '文件名已复制: ' + name,
                center: true,
                type: 'success'
            });
        },
    },
    watch: {
        // 监听路由变化，重新加载文件列表
        $route() {
            this.currentPage = 1; // 重置当前页
            if (this.$route.name === "all") {
                this.fetchFiles();
            }
            else {
                this.search();
            }
        },
        pageSize() {
            this.loadImg();
        },
        currentPage() {
            this.loadImg();
        }
    },
    mounted() {
        this.fetchFiles(); // 初始加载文件
        const contextMenu = document.getElementById('context-menu');
        document.addEventListener('click', () => {
            if (contextMenu != null) {
                contextMenu.style.display = 'none';
            }
        });
    }
};
</script>


<style scoped>
/* 文件页面 */
.all-file-view {
    display: flex;
    /* 使用弹性盒布局 */
    flex-direction: column;
    /* 垂直排列子元素 */
    height: 100vh;
    /* 高度设置为视口高度的 100% */
    overflow: hidden;
    /* 隐藏溢出内容，禁止页面滚动 */
}

/* 工具栏 */
.controls {
    display: flex;
    /* 使用弹性盒布局 */
    align-items: center;
    /* 子元素垂直居中 */
    gap: 10px;
    /* 子元素之间的间距为 10 像素 */
}

.controls>button {
    flex-basis: 8%;
    /* 每个子元素占据 10% 的宽度 */
    flex-shrink: 0;
    /* 禁止子元素缩小 */
    flex-grow: 0;
    /* 禁止子元素增长 */
}

.path-input,
.input-with-select {
    flex-basis: 18%;
    /* 每个子元素占据 18% 的宽度 */
    flex-shrink: 0;
    /* 禁止子元素缩小 */
    flex-grow: 0;
    /* 禁止子元素增长 */
}

.input-with-select,
.path-input {
    width: 150px;
    /* 固定宽度 */
}

/* 表格 */
.table-container {
    overflow: auto;
    /* 使内容可滚动 */
    flex-grow: 1;
    /* 占据剩余空间 */
}

.el-table {
    width: 100%;
    /* 表格宽度为容器的100% */
    height: calc(100vh - 180px);
    /* 表格高度根据视口高度计算 */
}

/* 分页 */
.pagination-container {
    display: flex;
    /* 使用弹性盒布局 */
    margin: 0px 180px 10px 10px;
    /* 外边距：顶部 0 像素，右侧 180 像素，底部 10 像素，左侧 10 像素 */
}

/* 缩略图 */
.thumbnail-all-view {
    height: calc(100vh - 180px);
    overflow: hidden;
    overflow: auto;
    /* 使内容可滚动 */
}

.thumbnail-view {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: flex-start;
    /* 子元素居左对齐 */
    align-items: flex-start;
    overflow: hidden;
    /* 子元素垂直居上对齐 */
}
.thumbnail {
    width: 120px;
    /* 固定宽度 */
    height: 150px;
    /* 固定高度 */
    margin: 10px;
    /* 外边距 10 像素 */
    display: flex;
    flex-direction: column;
    overflow: hidden;
    justify-content: flex-start;
    /* 子元素垂直居上对齐 */
}

.thumbnail-img {
    width: auto;
    height: 100px;
    cursor: pointer;
    object-fit: cover;
}

.file-name {
    margin-top: 5px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
    text-align: center;
    cursor: pointer;
}

/* 右键菜单 */
.context-menu {
    position: absolute;
    /* 绝对定位，用于在页面中精确定位元素 */
    z-index: 10000;
    /* 设置堆叠顺序，使菜单显示在其他元素之上 */
    background-color: #fff;
    /* 背景颜色为白色 */
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    /* 添加阴影效果，使菜单有浮动感 */
    border-radius: 0px;
    /* 圆角边框，使菜单边缘更柔和 */
    display: none;
    /* 默认隐藏菜单，只有在需要时才显示 */
}

.context-menu ul {
    list-style: none;
    /* 移除列表项的默认样式（如圆点或数字） */
    margin: 0;
    /* 移除默认的外边距 */
    padding: 0;
    /* 移除默认的内边距 */
    background-color: rgb(220, 223, 230)
}

.context-menu li {
    cursor: pointer;
    /* 鼠标悬停时的指针样式，表示菜单项是可点击的 */
    white-space: nowrap;
    /* 防止菜单项文本换行，确保文本在一行显示 */
}

.context-menu li:hover {
    background-color: #f5f5f5;
    /* 设置鼠标悬停时的背景颜色，使菜单项在悬停时突出显示 */
}
</style>