<template>
  <div class="book-container">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="书名/作者/ISBN"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="searchForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 150px"
          >
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
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
    
    <!-- 操作栏 -->
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>图书列表</span>
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">
          新增图书
        </el-button>
      </div>
      
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="index" width="50" align="center" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="title" label="书名" min-width="150" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="150" show-overflow-tooltip />
        <el-table-column prop="price" label="价格" width="80">
          <template slot-scope="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="库存" width="80" align="center" />
        <el-table-column prop="availableQuantity" label="可借" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.availableQuantity > 0 ? 'success' : 'danger'" size="small">
              {{ scope.row.availableQuantity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="text" size="small" @click="handleView(scope.row)">
              详情
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              style="color: #F5222D"
              @click="handleDelete(scope.row)"
            >
              删除
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
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
    >
      <el-form
        ref="form"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="书名" prop="title">
              <el-input v-model="form.title" placeholder="请输入书名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="item in categoryList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总库存" prop="totalQuantity">
              <el-input-number v-model="form.totalQuantity" :min="0" :precision="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="存放位置" prop="location">
              <el-input v-model="form.location" placeholder="如：A区-01-01" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="出版日期" prop="publishDate">
          <el-date-picker
            v-model="form.publishDate"
            type="date"
            placeholder="选择出版日期"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="图书简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图书简介"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBookList, addBook, updateBook, deleteBook } from '@/api/book'
import { getCategoryList } from '@/api/category'

export default {
  name: 'BookManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      categoryList: [],
      page: 1,
      size: 10,
      total: 0,
      searchForm: {
        keyword: '',
        categoryId: null
      },
      dialogVisible: false,
      dialogTitle: '新增图书',
      submitLoading: false,
      form: {
        id: null,
        isbn: '',
        title: '',
        author: '',
        publisher: '',
        categoryId: null,
        price: 0,
        totalQuantity: 0,
        availableQuantity: 0,
        location: '',
        publishDate: '',
        description: ''
      },
      rules: {
        isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
        title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
        author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        totalQuantity: [{ required: true, message: '请输入库存', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadCategories()
    this.loadData()
  },
  methods: {
    async loadCategories() {
      try {
        const res = await getCategoryList()
        if (res.code === 200) {
          this.categoryList = res.data
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    
    async loadData() {
      this.loading = true
      try {
        const res = await getBookList({
          page: this.page,
          size: this.size,
          keyword: this.searchForm.keyword,
          categoryId: this.searchForm.categoryId
        })
        if (res.code === 200) {
          this.tableData = res.data.records
          this.total = res.data.total
        }
      } catch (error) {
        console.error('加载图书失败:', error)
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
        keyword: '',
        categoryId: null
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
    
    handleAdd() {
      this.dialogTitle = '新增图书'
      this.form = {
        id: null,
        isbn: '',
        title: '',
        author: '',
        publisher: '',
        categoryId: null,
        price: 0,
        totalQuantity: 0,
        availableQuantity: 0,
        location: '',
        publishDate: '',
        description: ''
      }
      this.dialogVisible = true
    },
    
    handleEdit(row) {
      this.dialogTitle = '编辑图书'
      this.form = { ...row }
      this.dialogVisible = true
    },
    
    handleView(row) {
      this.$message.info('查看详情：' + row.title)
    },
    
    async handleDelete(row) {
      try {
        await this.$confirm(`确定要删除图书《${row.title}》吗？`, '提示', {
          type: 'warning'
        })
        
        const res = await deleteBook(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
        }
      }
    },
    
    handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          this.submitLoading = true
          try {
            const data = { ...this.form }
            if (!data.id) {
              data.availableQuantity = data.totalQuantity
            }
            
            const api = data.id ? updateBook(data.id, data) : addBook(data)
            const res = await api
            
            if (res.code === 200) {
              this.$message.success(data.id ? '更新成功' : '添加成功')
              this.dialogVisible = false
              this.loadData()
            }
          } catch (error) {
            console.error('提交失败:', error)
          } finally {
            this.submitLoading = false
          }
        }
      })
    },
    
    getStatusType(status) {
      const map = {
        0: 'info',
        1: 'success',
        2: 'warning'
      }
      return map[status] || 'info'
    },
    
    getStatusText(status) {
      const map = {
        0: '下架',
        1: '可借',
        2: '借完'
      }
      return map[status] || '未知'
    }
  }
}
</script>

<style scoped>
.book-container {
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
