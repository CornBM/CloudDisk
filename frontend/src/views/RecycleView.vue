<template>
    <div class="recycle-view">
        <el-row>
            <el-col :span="24">
                <el-input class="input-with-select-3" placeholder="请输入文件名" v-model="searchQuery"></el-input>
                <el-button type="primary" icon="el-icon-search" @click="search">
                    搜索
                </el-button>
                <el-button type="danger" icon="el-icon-delete" @click="batchDelete">
                    批量删除
                </el-button>
                <el-button type="success" icon="el-icon-refresh" @click="restoreFiles">
                    还原文件
                </el-button>
            </el-col>
        </el-row>

        <el-table :data="sortedFiles" style="width: 100%" @selection-change="handleSelectionChange"
            height="calc(100vh - 180px)" @sort-change="handleSortChange">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="name" label="文件名" width="180" sortable="custom"></el-table-column>
            <el-table-column prop="size" label="大小" width="100" sortable="custom"></el-table-column>
            <el-table-column prop="modified" label="删除时间" width="180" sortable="custom"></el-table-column>
            <el-table-column label="操作">
                <template slot-scope="scope">
                    <el-button @click="restoreFile(scope.row)" type="primary" size="mini">
                        还原
                    </el-button>
                    <el-button @click="deleteFile(scope.row)" type="danger" size="mini">
                        删除
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-pagination @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="currentPage"
            :page-sizes="[10, 20, 30, 40]" :page-size="pageSize" layout="total, sizes, prev, pager, next, jumper"
            :total="total"></el-pagination>
    </div>
</template>

<script>
export default {
    data() {
        return {
            searchQuery: '',
            files: [
                { name: '文件1', size: '2MB', modified: '2023-07-01 10:00' },
                { name: '文件2', size: '3MB', modified: '2023-07-02 11:00' },
                // 更多文件数据...
            ],
            selectedFiles: [],
            currentPage: 1,
            pageSize: 10,
            total: 2, // 总文件数
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
        }
    },
    methods: {
        search() {
            console.log('搜索:', this.searchQuery);
            // 执行搜索逻辑
        },
        batchDelete() {
            console.log('批量删除:', this.selectedFiles);
            // 执行批量删除逻辑
        },
        restoreFiles() {
            console.log('还原文件:', this.selectedFiles);
            // 执行还原文件逻辑
        },
        restoreFile(file) {
            console.log('还原文件:', file);
            // 执行还原单个文件逻辑
        },
        deleteFile(file) {
            console.log('删除文件:', file);
            // 弹出确认对话框，确认后删除文件
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
        }
    }
};
</script>

<style scoped>
.recycle-view {
    padding: 20px;
}
.input-with-select-3 {
    width: 300px;
    margin-right: 10px;
}
</style>
