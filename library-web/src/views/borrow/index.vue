<template>
  <div class="borrow-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="借阅状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="借阅中" :value="0" />
            <el-option label="已归还" :value="1" />
            <el-option label="逾期" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">
            搜索
          </el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 借阅列表 -->
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>借阅记录</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleBorrow">
          借阅图书
        </el-button>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="bookTitle" label="图书名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="bookIsbn" label="ISBN" width="140" />
        <el-table-column prop="realName" label="借阅人" width="100" />
        <el-table-column prop="borrowDate" label="借阅日期" width="120" />
        <el-table-column prop="dueDate" label="应还日期" width="120" />

        <el-table-column prop="status" label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button 
              v-if="scope.row.status === 0"
              type="text" 
              size="small"
              @click="handleReturn(scope.row)"
            >
              归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        :current-page="page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="size"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>
    
    <!-- 借阅对话框 -->
    <el-dialog
      title="借阅图书"
      :visible.sync="borrowDialogVisible"
      width="500px"
    >
      <el-form
        ref="borrowForm"
        :model="borrowForm"
        :rules="borrowRules"
        label-width="100px"
      >
        <el-form-item label="选择图书" prop="bookId">
          <el-select
            v-model="borrowForm.bookId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入图书名称搜索"
            :remote-method="searchBooks"
            :loading="bookSearchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in bookOptions"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            >
              <span>{{ item.title }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">
                可借: {{ item.availableQuantity }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="借阅天数" prop="borrowDays">
          <el-slider v-model="borrowForm.borrowDays" :max="60" show-stops :marks="{7:'7天', 30:'30天', 60:'60天'}"></el-slider>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleBorrowSubmit">
          确定借阅
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBorrowList, borrowBook, returnBook } from '@/api/borrow'
import { getBookList } from '@/api/book'

export default {
  name: 'BorrowManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      page: 1,
      size: 10,
      total: 0,
      searchForm: {
        status: null
      },
      borrowDialogVisible: false,
      borrowForm: {
        bookId: null,
        borrowDays: 30
      },
      borrowRules: {
        bookId: [{ required: true, message: '请选择图书', trigger: 'change' }]
      },
      submitLoading: false,
      bookSearchLoading: false,
      bookOptions: []
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getBorrowList({
          page: this.page,
          size: this.size,
          status: this.searchForm.status
        })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        console.error('加载借阅记录失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    handleSearch() {
      this.page = 1
      this.loadData()
    },
    
    handleReset() {
      this.searchForm = {
        status: null
      }
      this.handleSearch()
    },
    
    handleSizeChange(val) {
      this.size = val
      this.loadData()
    },
    
    handleCurrentChange(val) {
      this.page = val
      this.loadData()
    },
    
    handleBorrow() {
      this.borrowForm = {
        bookId: null,
        borrowDays: 30
      }
      this.bookOptions = []
      this.borrowDialogVisible = true
    },
    
    async searchBooks(query) {
      if (query !== '') {
        this.bookSearchLoading = true
        try {
          const res = await getBookList({
            page: 1,
            size: 20,
            keyword: query
          })
          if (res.code === 200) {
            this.bookOptions = res.data.records.filter(book => book.availableQuantity > 0)
          }
        } catch (error) {
          console.error('搜索图书失败:', error)
        } finally {
          this.bookSearchLoading = false
        }
      }
    },
    
    handleBorrowSubmit() {
      this.$refs.borrowForm.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            const res = await borrowBook(this.borrowForm.bookId, this.borrowForm.borrowDays)
            if (res.code === 200) {
              this.$message.success('借阅成功')
              this.borrowDialogVisible = false
              this.loadData()
            }
          } catch (error) {
            console.error('借阅失败:', error)
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    
    async handleReturn(row) {
      try {
        await this.$confirm(`确定要归还图书《${row.bookTitle}》吗？`, '提示', {
          type: 'warning'
        })
        
        const res = await returnBook(row.id)
        if (res.code === 200) {
          this.$message.success('归还成功')
          this.loadData()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('归还失败:', error)
        }
      }
    },
    
    getStatusType(status) {
      const map = {
        0: 'primary',
        1: 'success',
        2: 'danger'
      }
      return map[status] || 'info'
    },
    
    getStatusText(status) {
      const map = {
        0: '借阅中',
        1: '已归还',
        2: '逾期'
      }
      return map[status] || '未知'
    }
  }
}
</script>

<style scoped>
.borrow-container {
  padding-bottom: 24px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.table-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
}
</style>
