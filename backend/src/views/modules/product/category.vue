<template>
  <div class="main">
   <div class="btn-area">
     <el-switch
       id="switch"
       v-model="draggable"
       :width="40"
       active-text="开启拖拽">
     </el-switch>
     <el-button class="delete" type="danger" @click="batchDelete">批量删除</el-button>
     <el-button class="save" v-show="draggable" @click="batchSave">批量保存</el-button>
   </div>
    <el-progress type="circle"
                 class="circle"
                 v-show="isSubmit"
                 :text-inside="true"
                 :percentage="percentage"
                 status="success">
    </el-progress>
    <el-tree
             id="tree"
             :data="menus"
             :props="defaultProps"
             show-checkbox
             node-key="catId"
             :draggable="draggable"
             @node-drop="handleDrop"
             :allow-drop="allowDrop"
             ref="menuTree"
             :default-expanded-keys="key">
       <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level<3"
            type="text"
            size="mini"
            @click.stop="() => append(data)">
            添加
          </el-button>
          <el-button
            v-if="node.childNodes.length===0"
            type="text"
            size="mini"
            @click.stop="() => remove(node, data)">
            删除
          </el-button>
            <el-button
              type="text"
              size="mini"
              @click.stop="() => edit(node, data)">
            修改
          </el-button>
        </span>
      </span>
    </el-tree>
    <el-dialog
      :title="this.category.catId?'修改分类':'添加分类'"
      :visible.sync="dialogVisible"
      width="30%">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="doCategory">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'category',
  data() {
    return {
      pCid:[],
      percentage:0,
      isSubmit:false,
      draggable:false,
      maxLevel: 0,
      menus: [],
      defaultProps: {
        children: "children",
        label: "name"
      },
      key: [],
      dialogVisible: false,
      category: {
        catId: null,
        name: '',
        parentCid: 0,
        catLevel: 0,
        showStatus: 1,
        sort: 0,
        icon: '',
        productUnit: ''
      },
      updateNodes: []
    }
  },
  methods: {
    async getMenus() {
      const {data} = await this.$http.get(this.$http.adornUrl("/product/category/list/tree"))
      this.menus = data.data
    },
    async batchDelete(){
      let catIds=[]
      let checkedNodes=this.$refs.menuTree.getCheckedNodes()
      for (let i = 0; i < checkedNodes.length; i++) {
        catIds.push(checkedNodes[i].catId)
      }
      this.$confirm(`是否批量删除【${catIds.slice(0,7)}...】等项目?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const {data} = await this.$http.post(this.$http.adornUrl("/product/category/delete"), catIds)
        if (data.code === 0) {
          this.$message({
            type: 'success',
            message: '批量删除成功!'
          });
          this.getMenus().then();
        } else {
          this.$message({
            type: 'success',
            message: '批量删除失败!'
          });
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });
    },
    async batchSave(){
      this.$message({
        type: 'success',
        message: '数据正在保存中......'
      });
      this.menus=null
      this.percentage=0
      this.isSubmit=true
      let intervalId=setInterval(()=>{
        this.percentage++;
      },10)
      let {data} = await this.$http.post(this.$http.adornUrl(`/product/category/update/sort`), this.updateNodes)
      if (data.code === 0) {
        this.$message({
          type: 'success',
          message: '菜单顺序修改成功!'
        });
        this.percentage=100
        clearInterval(intervalId)
        this.getMenus().then()
        this.key = this.pCid
        this.maxLevel=0
        this.updateNodes=[]
        setTimeout(()=>{
          this.isSubmit=false
        },1000)
      }
    },
    handleDrop(draggingNode, dropNode, dropType, ev) {
      let siblings = null;
      let pId
      if (dropType === 'inner') {
        pId = dropNode.data.catId
        siblings = dropNode.childNodes
      } else {
        siblings = dropNode.parent.childNodes
        if (dropNode.parent.data instanceof Array) {
          pId = 0;
        } else {
          pId = dropNode.parent.data.catId
        }
      }
      for (let i = 0; i < siblings.length; i++) {
        if (siblings[i].data.catId === draggingNode.data.catId) {
          this.updateNodes.push({
            catId: siblings[i].data.catId,
            sort: i,
            parentCid: pId,
            catLevel:siblings[i].level
          })
          if (siblings[i].level!==draggingNode.level){
            this.updateChildNodeLevel(siblings[i]);
          }
        } else {
          this.updateNodes.push({catId: siblings[i].data.catId, sort: i})
        }
      }
      console.log(this.updateNodes)
      this.pCid.push(pId)
    },
    // async postUpdate(pId){
    //   let {data} = await this.$http.post(this.$http.adornUrl(`/product/category/update/sort`), this.updateNodes)
    //   if (data.code === 0) {
    //     this.$message({
    //       type: 'success',
    //       message: '菜单顺序修改成功!'
    //     });
    //     this.getMenus().then()
    //     this.key = [pId]
    //     this.maxLevel=0
    //     this.updateNodes=[]
    //   }
    // },
    updateChildNodeLevel(node) {
      if (node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          this.updateNodes.push({
            catId: node.childNodes[i].data.catId,
            catLevel: node.childNodes[i].level
          })
          this.updateChildNodeLevel(node.childNodes[i])
        }
      }
    },
    allowDrop(draggingNode, dropNode, type) {
      this.maxLevel=0
      this.countNodeLevel(draggingNode)
      let totalLevel = 0
      let level = this.maxLevel - draggingNode.level + 1
      if (type === 'inner') {
        totalLevel = level + dropNode.level
      } else {
        totalLevel = level + dropNode.parent.level
      }
      return totalLevel < 4
    },
    countNodeLevel(node) {
      if (node.childNodes != null && node.childNodes.length > 0) {
        for (let i = 0; i < node.childNodes.length; i++) {
          if (node.childNodes[i].level > this.maxLevel) {
            this.maxLevel = node.childNodes[i].level
          }
          this.countNodeLevel(node.childNodes[i])
        }
      }
    },
    async edit(node, d) {
      let {data} = await this.$http.get(this.$http.adornUrl(`/product/category/info/${d.catId}`), this.category)
      data = data.data
      console.log(data)
      this.dialogVisible = true
      this.category.catId = data.catId
      this.category.name = data.name
      this.category.parentCid = data.parentCid
      this.category.catLevel = data.catLevel
      this.category.icon = data.icon
      this.category.sort = data.sort
      this.category.showStatus = data.showStatus
      this.category.productUnit = data.productUnit
    },
    async append(data) {
      this.dialogVisible = true
      this.category.catId = null
      this.category.name = ''
      this.category.parentCid = data.catId
      this.category.catLevel = data.catLevel * 1 + 1
      this.category.icon = ''
      this.category.showStatus = 1
      this.category.productUnit = ''
    },
    async doCategory() {
      this.dialogVisible = false
      if (this.category.catId) {
        let {catId, name, icon, productUnit} = this.category
        let update = {catId, name, icon, productUnit}
        const {data} = await this.$http.post(this.$http.adornUrl("/product/category/update"), update)
        if (data.code === 0) {
          this.$message({
            type: 'success',
            message: '修改成功!'
          });
          this.getMenus().then()
          this.key = [this.category.parentCid]
        }
      } else {
        const {data} = await this.$http.post(this.$http.adornUrl("/product/category/save"), this.category)
        if (data.code === 0) {
          this.$message({
            type: 'success',
            message: '保存成功!'
          });
          this.getMenus().then()
          this.key = [this.category.parentCid]
        }
      }
    },
    async remove(node, treeData) {
      this.$confirm(`是否删除【${treeData.name}】项目?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        const {data} = await this.$http.post(this.$http.adornUrl("/product/category/delete"), [treeData.catId])
        if (data.code === 0) {
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
          this.getMenus().then();
          this.key = [node.parent.data.catId]
        } else {
          this.$message({
            type: 'success',
            message: '删除失败!'
          });
        }
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        });
      });

    }
  },
  created() {
    this.getMenus().then();
  }
}
</script>

<style scoped>
.el-switch{
  margin: 5px 10px;
  font-size: 20px;
}
#tree{
  margin-top: 20px;
}

.btn-area{
  position: relative;
  height: 20px;
}
.circle{
  margin: 20px;
  position: absolute;
  left: 50%;
  top: 250px;
  z-index: 99;
  transform: translatex(-100%);
}
.main::-webkit-scrollbar {display: none;}
</style>
