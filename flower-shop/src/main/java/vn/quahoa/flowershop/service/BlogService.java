package vn.quahoa.flowershop.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.quahoa.flowershop.dto.blog.BlogCreateRequest;
import vn.quahoa.flowershop.dto.blog.BlogUpdateRequest;
import vn.quahoa.flowershop.exception.ResourceNotFoundException;
import vn.quahoa.flowershop.model.Admin;
import vn.quahoa.flowershop.model.Blog;
import vn.quahoa.flowershop.model.Blog.BlogStatus;
import vn.quahoa.flowershop.model.BlogImage;
import vn.quahoa.flowershop.repository.AdminRepository;
import vn.quahoa.flowershop.repository.BlogImageRepository;
import vn.quahoa.flowershop.repository.BlogRepository;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final AdminRepository adminRepository;
    private final BlogImageRepository blogImageRepository;
    private final ImageStorageService imageStorageService;

    public Blog createBlog(BlogCreateRequest request) {
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setImageUrl(request.getImageUrl());
        blog.setSummary(request.getSummary());
        blog.setStatus(request.getStatus() != null ? request.getStatus() : BlogStatus.DRAFT);
        
        if (request.getAuthorId() != null) {
            Admin author = adminRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin", request.getAuthorId()));
            blog.setAuthor(author);
        }
        
        return blogRepository.save(blog);
    }

    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    public List<Blog> getPublishedBlogs() {
        return blogRepository.findByStatus(BlogStatus.PUBLISHED);
    }

    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", id));
    }

    public List<Blog> getBlogsByAuthor(Long authorId) {
        if (!adminRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Admin", authorId);
        }
        return blogRepository.findByAuthor_Id(authorId);
    }

    public List<Blog> searchBlogs(String keyword) {
        return blogRepository.searchPublishedBlogs(BlogStatus.PUBLISHED, keyword);
    }

    public Blog updateBlog(Long id, BlogUpdateRequest request) {
        Blog blog = getBlogById(id);
        
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setImageUrl(request.getImageUrl());
        blog.setSummary(request.getSummary());
        
        if (request.getStatus() != null) {
            blog.setStatus(request.getStatus());
        }
        
        return blogRepository.save(blog);
    }

    public void deleteBlog(Long id) {
        Blog blog = getBlogById(id);
        blogRepository.delete(blog);
    }

    public Blog publishBlog(Long id) {
        Blog blog = getBlogById(id);
        blog.setStatus(BlogStatus.PUBLISHED);
        return blogRepository.save(blog);
    }

    public Blog unpublishBlog(Long id) {
        Blog blog = getBlogById(id);
        blog.setStatus(BlogStatus.DRAFT);
        return blogRepository.save(blog);
    }

    // ============================================
    // IMAGE MANAGEMENT METHODS
    // ============================================

    /**
     * Upload main/featured image from file
     */
    public String uploadMainBlogImage(Long blogId, MultipartFile file) {
        Blog blog = getBlogById(blogId);

        try {
            String imageUrl = imageStorageService.saveImageFromFile(file, blogId, false);
            blog.setImageUrl(imageUrl);
            blogRepository.save(blog);
            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload main blog image", e);
        }
    }

    /**
     * Upload main/featured image from URL
     */
    public String uploadMainBlogImageFromUrl(Long blogId, String imageUrl) {
        Blog blog = getBlogById(blogId);

        try {
            String savedImageUrl = imageStorageService.saveImageFromUrl(imageUrl, blogId, false);
            blog.setImageUrl(savedImageUrl);
            blogRepository.save(blog);
            return savedImageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload main blog image from URL", e);
        }
    }

    /**
     * Upload additional image from file
     */
    public String uploadBlogImage(Long blogId, MultipartFile file) {
        Blog blog = getBlogById(blogId);

        try {
            String imageUrl = imageStorageService.saveImageFromFile(file, blogId, false);

            BlogImage blogImage = new BlogImage();
            blogImage.setFileName(file.getOriginalFilename());
            blogImage.setFilePath(imageUrl);
            blogImage.setImageUrl(imageUrl);
            blogImage.setBlog(blog);
            blog.getImages().add(blogImage);

            blogRepository.save(blog);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload blog image", e);
        }
    }

    /**
     * Upload additional image from URL
     */
    public String uploadBlogImageFromUrl(Long blogId, String imageUrl) {
        Blog blog = getBlogById(blogId);

        try {
            String savedImageUrl = imageStorageService.saveImageFromUrl(imageUrl, blogId, false);

            BlogImage blogImage = new BlogImage();
            blogImage.setFileName("URL Image");
            blogImage.setFilePath(savedImageUrl);
            blogImage.setImageUrl(savedImageUrl);
            blogImage.setBlog(blog);
            blog.getImages().add(blogImage);

            blogRepository.save(blog);

            return savedImageUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload blog image from URL", e);
        }
    }

    /**
     * Delete main/featured image
     */
    public void deleteMainBlogImage(Long blogId) {
        Blog blog = getBlogById(blogId);
        
        if (blog.getImageUrl() != null) {
            imageStorageService.deleteImage(blog.getImageUrl());
            blog.setImageUrl(null);
            blogRepository.save(blog);
        }
    }

    /**
     * Delete a specific additional image
     */
    public void deleteBlogImage(Long blogId, Long imageId) {
        Blog blog = getBlogById(blogId);
        
        BlogImage image = blogImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("BlogImage", imageId));

        if (!image.getBlog().getId().equals(blogId)) {
            throw new IllegalArgumentException("Image does not belong to this blog");
        }

        imageStorageService.deleteImage(image.getImageUrl());
        blog.getImages().remove(image);
        blogImageRepository.delete(image);
    }

    /**
     * Delete all additional images for a blog
     */
    public void deleteAllBlogImages(Long blogId) {
        Blog blog = getBlogById(blogId);

        for (BlogImage image : blog.getImages()) {
            imageStorageService.deleteImage(image.getImageUrl());
        }

        blog.getImages().clear();
        blogRepository.save(blog);
    }
}
