import { defineStore } from 'pinia'

export const useMemberStore = defineStore('member', {
  state: () => ({
    currentMember: JSON.parse(localStorage.getItem('currentMember') || 'null'),
  }),
  actions: {
    setCurrentMember(member) {
      this.currentMember = member
      localStorage.setItem('currentMember', JSON.stringify(member))
    },
    clearCurrentMember() {
      this.currentMember = null
      localStorage.removeItem('currentMember')
    },
  },
})
