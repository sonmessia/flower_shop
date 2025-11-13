package vn.quahoa.flowershop.dto.blog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import vn.quahoa.flowershop.model.Blog;
import vn.quahoa.flowershop.model.Blog.BlogStatus;
import vn.quahoa.flowershop.model.BlogImage;

@Data
public class BlogResponse {
    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String summary;
    private BlogStatus status;
    private Long authorId;
    private String authorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BlogImageResponse> images;

    public static BlogResponse fromEntity(Blog blog) {
        BlogResponse response = new BlogResponse();
        response.setId(blog.getId());
        response.setTitle(blog.getTitle());
        response.setContent(blog.getContent());
        response.setImageUrl(blog.getImageUrl());
        response.setSummary(blog.getSummary());
        response.setStatus(blog.getStatus());
        response.setCreatedAt(blog.getCreatedAt());
        response.setUpdatedAt(blog.getUpdatedAt());
        
        if (blog.getAuthor() != null) {
            response.setAuthorId(blog.getAuthor().getId());
            response.setAuthorUsername(blog.getAuthor().getUsername());
        }

        if (blog.getImages() != null && !blog.getImages().isEmpty()) {
            response.setImages(blog.getImages().stream()
                    .map(BlogImageResponse::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }

    @Data
    public static class BlogImageResponse {
        private Long id;
        private String imageUrl;
        private String fileName;
        private Integer displayOrder;

        public static BlogImageResponse fromEntity(BlogImage image) {
            BlogImageResponse response = new BlogImageResponse();
            response.setId(image.getId());
            response.setImageUrl(image.getImageUrl());
            response.setFileName(image.getFileName());
            response.setDisplayOrder(image.getDisplayOrder());
            return response;
        }
    }
}
