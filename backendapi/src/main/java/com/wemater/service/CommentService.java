package com.wemater.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.hibernate.SessionFactory;

import com.wemater.dao.ArticleDao;
import com.wemater.dao.CommentDao;
import com.wemater.dao.UserDao;
import com.wemater.dto.Article;
import com.wemater.dto.Comment;
import com.wemater.dto.User;
import com.wemater.modal.CommentModel;
import com.wemater.modal.Link;
import com.wemater.util.HibernateUtil;
import com.wemater.util.SessionUtil;

public class CommentService {
	
	private  final SessionFactory sessionfactory;
    private final SessionUtil su;
    private final CommentDao cd;
    private final ArticleDao ad;
    private final UserDao ud;
   
   
 
	public CommentService() {
		this.sessionfactory = HibernateUtil.getSessionFactory();
		this.su = new SessionUtil(sessionfactory.openSession());
		this.cd = new CommentDao(su);
		this.ud = new UserDao(su);
		this.ad = new ArticleDao(su);
	  
	}

	
	//1: get all Comments of User
    public List<CommentModel> getAlluserComments(String username,UriInfo uriInfo){
   
    	//authentication here
   		return transformUsersCommentsToModelsWithUsername(
   				                  cd.getAllCommentsOfUserByNamedQuery(username), uriInfo);

    }


    
	
  //2: get one Comments of User
	public CommentModel getOneuserComment(long commentId, UriInfo uriInfo){

	//authentication heere profilename is same as loggedin user	
   	  String profilename = HibernateUtil.getUsernameFromURLforComments(3,uriInfo);
   	   
   	 if(cd.IsUserCommentAvailable(profilename, commentId))
   	 
   		 return transformCommentToModelForUser(cd.getCommentOfUserByNamedQuery(profilename, commentId),uriInfo);
   	 
   	  else return null;
    }

  
     
  
    
  //3: getallComments on an article of my article
 
    public List<CommentModel> getAllArticleComments(Long articleId, UriInfo uriInfo){
    	
    	String profilename = HibernateUtil.getUsernameFromURLforComments(4, uriInfo);
	      	 if( ad.IsUserArticleAvailable(profilename, articleId) ){
    	
    	        return transformUsersCommentsToModelsWithArticleId(
    			                    cd.getAllCommentsOfArticleByNamedQuery(articleId), uriInfo);
	      	 }
	      	 else return null;
    } 
    
    //4: get one article comment not allowed
    
    public CommentModel getarticleComment(long commentId, UriInfo uriInfo){
    	/*auhtenticate the user and if same username
    	 * 
    	 * then show the comment
    	 * condition is:  user has posted this comment and the article provided in url has this comment
    	 */
    	long articleId = HibernateUtil.getArticleIdFromURLforComments(3, uriInfo);			
    	String profname = HibernateUtil.getUsernameFromURLforComments(5, uriInfo);
    	
    	if(cd.IsUserCommentAvailable(profname, commentId)
    			&& cd.IsArticleCommentAvailable(commentId,articleId)){
    		
    		Comment comment = cd.find(commentId);
    		return transformCommentToModelForUser(comment, uriInfo);
 
    	}
    	else return null;

    	
    }
    
    
    //5: post comments.
    
    public CommentModel postArticleComment(CommentModel model, UriInfo uriInfo){
   	      	 
    	Long articleId = HibernateUtil.getArticleIdFromURLforComments(2,uriInfo);
   	    String profilename = HibernateUtil.getUsernameFromURLforComments(4, uriInfo);
   	    
	   	        User user = ud.find(profilename); //get user
	   	        Article article = ad.find(articleId); //get article
	   	        Comment comment = cd.createComment(model.getContent(), article, user); //get comment
	   	        long id = cd.save(comment); //save article or exception will be thrown
	   	        
	   	        return transformCommentToModelForArticle(cd.find(id), uriInfo); //return model of comment   	  
    }
    //5: Update the comment in article for any ones articles
    
    public CommentModel UpdateArticleComment(long commentId,CommentModel model, UriInfo uriInfo){
	      	 
    	 String profilename = HibernateUtil.getUsernameFromURLforComments(5, uriInfo);

   	    
   	     if( cd.IsUserCommentAvailable(profilename, commentId)){
	   	      
	   	        Comment comment = cd.find(commentId);
	   	         comment.setContent(model.getContent());
	   	         cd.save(comment);
	   	        
	   	        return transformCommentToModelForArticle(cd.find(commentId), uriInfo); //return model of comment
	  
	   	    }
	   	    
   	      else return null;    	  
    } 
    
    
    //6: delete the comment
    
    
    public void deleteArticleComment(long commentId, UriInfo uriInfo){
   	      	 String profilename = HibernateUtil.getUsernameFromURLforComments(5, uriInfo);
   	      	 
   	      	 if(cd.IsUserCommentAvailable(profilename, commentId) ){
   	      	 
   	      		 Comment comment = cd.find(commentId);
   	      		 cd.delete(comment);
   	      		 
   	      	 }
   	      	
    }
    
    
    
    
    
    
    
	//comment service starts here
    
    
    //get all users
	private List<CommentModel> transformUsersCommentsToModelsWithArticleId(List<Comment> comments, UriInfo uriInfo) {
		
		 List<CommentModel> models = new ArrayList<CommentModel>();
		 
		 for (Iterator<Comment> iterator = comments.iterator(); iterator.hasNext();) {
	           Comment comment = iterator.next();
			   CommentModel model = transformCommentToModelForArticle(comment, uriInfo);
			   models.add(model);
			 }

		return models;
	}
    
	
	private List<CommentModel> transformUsersCommentsToModelsWithUsername(List<Comment> comments, UriInfo uriInfo) {
		
		 List<CommentModel> models = new ArrayList<CommentModel>();
		 
		 for (Iterator<Comment> iterator = comments.iterator(); iterator.hasNext();) {
	
			  Comment comment = iterator.next();
			   CommentModel model = transformCommentToModelForUser(comment, uriInfo);
			   models.add(model);
			
		 }  

		return models;
	}	
	

	//transform Comment to model for user -- for usercomments
	private CommentModel transformCommentToModelForUser(Comment comment, UriInfo uriInfo) {
		
			Link self = LinkService.createLinkForEachUserComment("getComments",
					                                       comment.getUsername(),
					                                       comment.getId(),
					                                       uriInfo,"self");		
			Link comments = LinkService.createLinkForUserComments("getComments",
					                                           comment.getUsername(),
					                                           uriInfo,
					                                           "comments");
			Link user = LinkService.CreateLinkForEachUser(comment.getUsername(),
					                                    uriInfo,
					                                    "user");
			
			Link article = LinkService.createLinkForEachArticleOfUser("getAllArticles",
			                                                       comment.getArticle().getUser().getUsername(),
			                                                       comment.getArticle().getId(), uriInfo,
			                                                       "article");

		

			
			CommentModel model = new CommentModel()
		                       .ConstructModel(comment)
		 		               .addArticle(comment.getArticle())
		 		               .addUser(comment.getUser())
		                       .addLinks(self, user,article,comments);
		
			return model;
	}


	//transform Comment to model for user -- for Article
		private CommentModel transformCommentToModelForArticle(Comment comment, UriInfo uriInfo) {
			
				Link self = LinkService.createLinkForEachUserComment("getComments",
						                                       comment.getUsername(),
						                                       comment.getId(),
						                                       uriInfo,"self");		
				Link comments = LinkService.createLinkForArticleComments("getAllArticles",
																	  "getAllComments",
																	  comment.getArticle().getUser().getUsername(),
																	  comment.getArticle().getId(),
																	  uriInfo,
																	   "comments");
				Link user = LinkService.CreateLinkForEachUser(comment.getUsername(),
						                                    uriInfo,
						                                    "user");
				
				Link article = LinkService.createLinkForEachArticleOfUser("getAllArticles",
				                                                       comment.getArticle().getUser().getUsername(),
				                                                       comment.getArticle().getId(), uriInfo,
				                                                       "article");

			

				
				CommentModel model = new CommentModel()
			                       .ConstructModel(comment)
			 		               .addArticle(comment.getArticle())
			 		               .addUser(comment.getUser())
			                       .addLinks(self, user,article,comments);
			
				return model;
		}
		
	
		
	
		  
}
