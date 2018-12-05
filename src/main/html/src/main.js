import Vue from 'vue/dist/vue.js'
import VueRouter from 'vue-router'

//import cognitoAuth from './cognito'

import createSchedule from './components/createSchedule'
import viewSchedule from './components/viewSchedule'
import login from './components/login'
import sysAdminSearch from './components/sysAdminSearch'
import searchSchedule from './components/searchSchedule'
import editSchedule from './components/editSchedule'

import './style/base.scss'

Vue.use(VueRouter);

let router = new VueRouter({
  routes: [
	{
		path: '/createSchedule',
		component: createSchedule
    },{
    	path: '/viewSchedule',
    	component: viewSchedule
    },{
    	path: '/login',
    	component: login
    },{
      path: '/sysAdminSearch',
      component: sysAdminSearch
    },{
      path: '/searchSchedule',
      component: searchSchedule
    },{
      path: '/editSchedule',
      component: editSchedule
    }
  ]
});

new Vue({
  el: '#app',
  router
  //cognitoAuth
})