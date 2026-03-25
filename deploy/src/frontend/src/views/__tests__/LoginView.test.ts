import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoginView from '@/views/LoginView.vue'

describe('LoginView', () => {
  it('renders login form', () => {
    const wrapper = mount(LoginView)
    expect(wrapper.find('input[type="text"]').exists()).toBe(true)
    expect(wrapper.find('input[type="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
  })

  it('has default username "admin"', () => {
    const wrapper = mount(LoginView)
    const usernameInput = wrapper.find('input[type="text"]').element as HTMLInputElement
    expect(usernameInput.value).toBe('admin')
  })
})
