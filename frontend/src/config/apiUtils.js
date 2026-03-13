// API utility functions for making HTTP requests
import API from './api'
import axiosInstance from './axiosConfig'

/**
 * Get auth headers from localStorage
 */
function getAuthHeaders() {
  const adminData = localStorage.getItem('admin')
  if (adminData) {
    try {
      const admin = JSON.parse(adminData)
      if (admin.accessToken) {
        return { 'Authorization': `Bearer ${admin.accessToken}` }
      }
    } catch (e) {
      console.error('Error parsing admin data:', e)
    }
  }
  return {}
}

/**
 * Upload main product image
 * @param {number} productId - Product ID
 * @param {File} file - Image file
 * @returns {Promise<string>} - Image URL
 */
export async function uploadMainProductImage(productId, file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(API.products.uploadMainImage(productId), {
    method: 'POST',
    headers: getAuthHeaders(),
    body: formData,
  })

  if (!response.ok) {
    throw new Error('Failed to upload main product image')
  }

  return await response.text()
}

/**
 * Upload additional product image
 * @param {number} productId - Product ID
 * @param {File} file - Image file
 * @returns {Promise<string>} - Image URL
 */
export async function uploadProductImage(productId, file) {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(API.products.uploadImage(productId), {
    method: 'POST',
    headers: getAuthHeaders(),
    body: formData,
  })

  if (!response.ok) {
    throw new Error('Failed to upload product image')
  }

  return await response.text()
}

/**
 * Delete main product image
 * @param {number} productId - Product ID
 */
export async function deleteMainProductImage(productId) {
  const response = await fetch(API.products.deleteMainImage(productId), {
    method: 'DELETE',
    headers: getAuthHeaders(),
  })

  if (!response.ok) {
    throw new Error('Failed to delete main product image')
  }
}

/**
 * Delete product image
 * @param {number} productId - Product ID
 * @param {number} imageId - Image ID
 */
export async function deleteProductImage(productId, imageId) {
  const response = await fetch(API.products.deleteImage(productId, imageId), {
    method: 'DELETE',
    headers: getAuthHeaders(),
  })

  if (!response.ok) {
    throw new Error('Failed to delete product image')
  }
}

/**
 * Delete all product images
 * @param {number} productId - Product ID
 */
export async function deleteAllProductImages(productId) {
  const response = await fetch(API.products.deleteAllImages(productId), {
    method: 'DELETE',
    headers: getAuthHeaders(),
  })

  if (!response.ok) {
    throw new Error('Failed to delete all product images')
  }
}

/**
 * Upload main blog image
 * @param {number} blogId - Blog ID
 * @param {File} file - Image file
 * @param {string} token - Authorization token (optional, will use localStorage if not provided)
 * @returns {Promise<string>} - Image URL
 */
export async function uploadMainBlogImage(blogId, file, token) {
  const formData = new FormData()
  formData.append('file', file)

  const headers = token
    ? { 'Authorization': `Bearer ${token}` }
    : getAuthHeaders()

  const response = await fetch(API.blogs.uploadMainImage(blogId), {
    method: 'POST',
    headers: headers,
    body: formData,
  })

  if (!response.ok) {
    throw new Error('Failed to upload main blog image')
  }
  console.log("Main blog image upload response:", response);
  return await response.text()
}

/**
 * Upload additional blog image
 * @param {number} blogId - Blog ID
 * @param {File} file - Image file
 * @param {string} token - Authorization token (optional, will use localStorage if not provided)
 * @returns {Promise<string>} - Image URL
 */
export async function uploadBlogImage(blogId, file, token) {
  const formData = new FormData()
  formData.append('file', file)

  const headers = token
    ? { 'Authorization': `Bearer ${token}` }
    : getAuthHeaders()

  const response = await fetch(API.blogs.uploadImage(blogId), {
    method: 'POST',
    headers: headers,
    body: formData,
  })

  if (!response.ok) {
    throw new Error('Failed to upload blog image')
  }

  return await response.text()
}

// Export axios instance for direct use
export { axiosInstance }
