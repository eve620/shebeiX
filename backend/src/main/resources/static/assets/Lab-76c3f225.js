import{_ as oe,g as c,u as se,o as de,z as L,h as f,b as h,c as A,i as l,j as o,w as I,k as E,d as U,F as B,m as g,l as R,q as V,B as re,r as ce,x as M,s as q,t as $}from"./index-03f9d86f.js";import{g as ie}from"./Instance-e9cab15d.js";import{O as ve,d as me,D as fe}from"./exportToExcel-9b58f540.js";import{e as pe}from"./utils-eab88610.js";const _e={class:"loading"},be={key:0},he=["onClick"],ge=["onClick"],Ne={__name:"Lab",setup(ke){const r=ie();let D,p;const u=c({}),y=c(!1),N=c(!1);se();const F=a=>{u.value.userAccount=a},T=async()=>{u.value={},p===void 0&&(p=(await r.getAllUserList()).data.data),N.value=!0},z=a=>{ce.push({name:"labDetail",query:{id:pe(a)}})},_=c(),J=()=>{_.value.validateFields().then(a=>{s.value=!1,r.addLab(u.value).then(e=>{e.data.code===1?(g.info(e.data.data),r.getLabList().then(n=>{v.value=n.data.data,i.value=n.data.data,s.value=!0})):(g.info(e.data.msg),s.value=!0)}),N.value=!1}).catch(a=>{console.log("Validate Failed:",a)})},j=()=>{_.value.resetFields(),N.value=!1},k=c(!1),H=async a=>{u.value=JSON.parse(JSON.stringify(a)),p===void 0&&(p=(await r.getAllUserList()).data.data),k.value=!0},G=()=>{_.value.validateFields().then(a=>{s.value=!1,r.editLab(u.value).then(e=>{e.data.code===1?(g.info(e.data.data),r.getLabList().then(n=>{v.value=n.data.data,i.value=n.data.data,s.value=!0})):(g.info(e.data.msg),s.value=!0)}),k.value=!1}).catch(a=>{console.log("Validate Failed:",a)})},K=()=>{_.value.resetFields(),k.value=!1},Q=a=>{s.value=!1,r.deleteLabById(a).then(e=>{e.data.code===1?(g.info(e.data.data),r.getLabList().then(n=>{v.value=n.data.data,i.value=n.data.data,s.value=!0})):g.info(e.data.msg)})};de(()=>{r.whoami().then(a=>{a.data.code===1&&(D=a.data.data,y.value=D.roleId)}),r.getLabList().then(a=>{v.value=a.data.data,i.value=a.data.data,s.value=!0})});const W=()=>{me("/lab",i.value,"实验室表.xlsx")},i=c(),v=c([]),s=c(!1),X=L(()=>new Set(v.value.map(a=>a.labName))),Y=L(()=>{let a=[];return X.value.forEach(e=>{a.push({value:e})}),a}),Z=L(()=>new Set(v.value.map(a=>a.userName))),P=L(()=>{let a=[];return Z.value.forEach(e=>{a.push({value:e})}),a}),w=c([]),C=c([]),ee=a=>{s.value=!1,i.value=v.value.filter(e=>{w.value=a.itemSelected,C.value=a.userSelected;const n=w.value.length===0||w.value.some(m=>e.labName.includes(m)),d=C.value.length===0||C.value.some(m=>e.userName.includes(m));return n&&d}),s.value=!0},ae=(a,e,n)=>{i.value=v.value.filter(d=>{const m=w.value.length===0||w.value.some(b=>d.itemName.includes(b)),x=C.value.length===0||C.value.some(b=>d.userName.includes(b));return m&&x})},te=[{title:"实验室名称",dataIndex:"labName",width:"15%"},{title:"管理员名称",dataIndex:"userName",width:"15%"},{title:"管理员工资号",dataIndex:"userAccount",width:"40%"},{title:"操作",dataIndex:"operation"}];return(a,e)=>{const n=f("a-input"),d=f("a-form-item"),m=f("a-select-option"),x=f("a-select"),b=f("a-form"),O=f("a-modal"),le=f("a-spin"),ue=f("a-table");return h(),A(B,null,[l(O,{open:N.value,"onUpdate:open":e[3]||(e[3]=t=>N.value=t),"ok-text":"确定","cancel-text":"取消",onOk:J,onCancel:j,title:"添加"},{default:o(()=>[l(b,{ref_key:"formRef",ref:_,model:u.value,labelCol:{span:6},wrapperCol:{span:14}},{default:o(()=>[l(d,{name:"labName",label:"地点",rules:[{required:!0,message:"请输入地点"}]},{default:o(()=>[l(n,{value:u.value.labName,"onUpdate:value":e[0]||(e[0]=t=>u.value.labName=t),placeholder:"请输入地点"},null,8,["value"])]),_:1}),l(d,{name:"userName",label:"管理人",rules:[{required:!0,message:"请选择管理人"}]},{default:o(()=>[l(x,{value:u.value.userName,"onUpdate:value":e[1]||(e[1]=t=>u.value.userName=t),placeholder:"请选择"},{default:o(()=>[(h(!0),A(B,null,R(V(p),t=>(h(),M(m,{value:t.userName,onClick:S=>F(t.userAccount)},{default:o(()=>[q($(t.userName),1)]),_:2},1032,["value","onClick"]))),256))]),_:1},8,["value"])]),_:1}),l(d,{label:"工资号"},{default:o(()=>[l(n,{value:u.value.userAccount,"onUpdate:value":e[2]||(e[2]=t=>u.value.userAccount=t),disabled:!0},null,8,["value"])]),_:1})]),_:1},8,["model"])]),_:1},8,["open"]),l(O,{open:k.value,"onUpdate:open":e[7]||(e[7]=t=>k.value=t),"ok-text":"确定","cancel-text":"取消",onOk:G,onCancel:K,title:"编辑"},{default:o(()=>[l(b,{ref_key:"formRef",ref:_,model:u.value,labelCol:{span:6},wrapperCol:{span:14}},{default:o(()=>[l(d,{label:"地点"},{default:o(()=>[l(n,{value:u.value.labName,"onUpdate:value":e[4]||(e[4]=t=>u.value.labName=t),disabled:!0},null,8,["value"])]),_:1}),l(d,{label:"管理人"},{default:o(()=>[l(x,{value:u.value.userName,"onUpdate:value":e[5]||(e[5]=t=>u.value.userName=t),placeholder:"请选择"},{default:o(()=>[(h(!0),A(B,null,R(V(p),t=>(h(),M(m,{value:t.userName,onClick:S=>F(t.userAccount)},{default:o(()=>[q($(t.userName),1)]),_:2},1032,["value","onClick"]))),256))]),_:1},8,["value"])]),_:1}),l(d,{label:"工资号"},{default:o(()=>[l(n,{value:u.value.userAccount,"onUpdate:value":e[6]||(e[6]=t=>u.value.userAccount=t),disabled:!0},null,8,["value"])]),_:1})]),_:1},8,["model"])]),_:1},8,["open"]),l(ve,{"item-list":Y.value,"user-list":P.value,addShow:y.value,onAdd:T,onExport:W,onHandleSearch:ee},null,8,["item-list","user-list","addShow"]),I(U("div",_e,[l(le,{size:"large"})],512),[[E,!s.value]]),I(l(ue,{columns:te,"data-source":i.value,onChange:ae,"row-key":"itemId",bordered:""},{bodyCell:o(({column:t,record:S})=>[t.dataIndex==="operation"?(h(),A("span",be,[U("a",{onClick:ne=>H(S)},"编辑",8,he),U("a",{class:"check-button",onClick:ne=>z(S.labName)},"查看",8,ge),I(l(fe,{onDelete:Q,tagetId:S.labId},null,8,["tagetId"]),[[E,y.value]])])):re("",!0)]),_:1},8,["data-source"]),[[E,s.value]])],64)}}},Le=oe(Ne,[["__scopeId","data-v-c1bb55b9"]]);export{Le as default};