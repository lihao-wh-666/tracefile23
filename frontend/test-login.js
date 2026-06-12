import axios from 'axios'
import { JSEncrypt } from 'jsencrypt'

const BASE_URL = 'http://localhost:6080/api'

async function testLogin() {
  try {
    console.log('=== Step 1: Get public key ===')
    const pubKeyResp = await axios.get(`${BASE_URL}/auth/public-key`)
    console.log('Public key response code:', pubKeyResp.data.code)
    const publicKey = pubKeyResp.data.data.publicKey
    console.log('Public key:', publicKey.substring(0, 30) + '...')

    console.log('\n=== Step 2: Encrypt password ===')
    const encryptor = new JSEncrypt()
    encryptor.setPublicKey(publicKey)
    const encryptedPassword = encryptor.encrypt('admin123')
    console.log('Encrypted password:', encryptedPassword ? encryptedPassword.substring(0, 30) + '...' : 'FAILED')
    if (!encryptedPassword) {
      console.error('Password encryption FAILED!')
      return
    }

    console.log('\n=== Step 3: Login ===')
    const loginResp = await axios.post(`${BASE_URL}/auth/login`, {
      username: 'admin',
      password: encryptedPassword
    })
    console.log('Login response:', JSON.stringify(loginResp.data, null, 2))
    if (loginResp.data.code !== 200) {
      console.error('Login FAILED!')
      return
    }
    const token = loginResp.data.data.token
    console.log('\nToken:', token.substring(0, 30) + '...')

    console.log('\n=== Step 4: Get user info ===')
    try {
      const userInfoResp = await axios.get(`${BASE_URL}/user/info`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      console.log('User info response:', JSON.stringify(userInfoResp.data, null, 2))
    } catch (err) {
      console.error('User info request FAILED!')
      console.error('Status:', err.response?.status)
      console.error('Data:', JSON.stringify(err.response?.data, null, 2))
    }

    console.log('\n=== Step 5: Logout ===')
    try {
      const logoutResp = await axios.post(`${BASE_URL}/auth/logout`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      })
      console.log('Logout response:', JSON.stringify(logoutResp.data, null, 2))
    } catch (err) {
      console.error('Logout request FAILED!')
      console.error('Status:', err.response?.status)
      console.error('Data:', JSON.stringify(err.response?.data, null, 2))
    }

  } catch (err) {
    console.error('Fatal error:', err.message)
    if (err.response) {
      console.error('Response status:', err.response.status)
      console.error('Response data:', err.response.data)
    }
  }
}

testLogin()
