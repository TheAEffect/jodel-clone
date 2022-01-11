package de.hse.jodel.controller;

import java.util.*;
import java.util.regex.Pattern;
import java.util.Base64;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import de.hse.jodel.model.*;
import de.hse.jodel.utils.AuthUser;
import de.hse.jodel.utils.exception.HttpExceptions;
import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;


@ApplicationScoped
public class PostController {

    @Inject
    EntityManager em;

    @Inject
    AuthUser authUser;

    private static final Logger LOGGER = Logger.getLogger(PostController.class);

    /**
     * Gets posts
     * @param latitude latitude
     * @param longitude longitude
     * @return list of all posts given by data
     */
    public List<Post> getPosts(String criteria, double latitude, double longitude) {
        User user = authUser.getUser();
        List<Post> posts = null;
        int oldSize = 0;
        double radius = 0.00;
        boolean changed;

        final String HAVERSINE = "(6371000 * acos( " +
                "cos(radians( :latitude )) " +
                "* cos(radians( p.latitude )) " +
                "* cos(radians( :longitude ) - radians( p.longitude )) " +
                "+ sin(radians( :latitude )) " +
                "* sin(radians( p.latitude )) " +
                "))";
        final String query = "SELECT p FROM Post p WHERE " + HAVERSINE + "  < :radius"; // base query
        int counter = 0;
        try {
            do {
                changed = false;
                radius += 5000.00;
                Map<String, Object> params = new HashMap<>();
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("radius", radius);
                if("comments".equals(criteria)) {
                    final String query2 = " ORDER BY p.comment_number DESC, p.id DESC";
                    posts = Post.find(query + query2, params).range(0, 50).list();
                } else if("votes".equals(criteria)) {
                    final String query2 = " ORDER BY p.votingValue DESC, p.id DESC";
                    posts = Post.find(query+query2, params).range(0, 50).list();
                } else {
                    final String query2 = " ORDER BY p.id DESC";
                    posts = Post.find(query+query2, params).range(0, 50).list();
                }
                //(double) radius).range(startIndex, stopIndex).list();
                if (posts.size() > oldSize && counter > 20) {
                    oldSize = posts.size();
                    changed = true;
                }

                for (Post post : posts) {
                    if (post.user.equals(user)) {
                        post.yours = true;
                    }

                    Date d = new Date();
                    boolean isExpired = (d.getTime() - post.postedAt.getTime()) > 86400000;

                    if(Post.TYPE.SURVEY.equals(post.type)) {
                        post.surveys = SurveyOption.findSurveysOfPostId(post.id, user);
                        SurveyVote vote = SurveyVote.findVote(user, post.id);
                        post.vote_id = vote == null ? null : vote.options_id;
                        post.survey_votes = SurveyVote.findVote(user, post.id) == null && !isExpired? null: post.survey_votes;
                    }

                    for (Voting vote : post.votings) {
                        if (vote.user.equals(user)) {
                            vote.yours = true;
                        }
                    }
                }
                //while autoDistance enabled, postsize is max 50 AND (the posts have increases OR the loop reached 20)
            } while (user.autoDistance && posts.size() < 50 && (++counter < 20 || changed));
            return posts;
        } catch (SecurityException | IllegalStateException e) {
            e.printStackTrace();
        }

        return Post.listAll(Sort.descending("id"));
    }

    /**
     * Gets posts
     * @param latitude latitude
     * @param longitude longitude
     * @return list of all posts given by data
     */
    public List<Post> getPosts(long channelid, String criteria, double latitude, double longitude) {
        User user = authUser.getUser();
        List<Post> posts = null;
        List<Post> postsFiltered = new ArrayList<> ();
        int oldSize = 0;
        double radius = 0.00;
        boolean changed;

        final String HAVERSINE = "(6371000 * acos( " +
                "cos(radians( :latitude )) " +
                "* cos(radians( p.latitude )) " +
                "* cos(radians( :longitude ) - radians( p.longitude )) " +
                "+ sin(radians( :latitude )) " +
                "* sin(radians( p.latitude )) " +
                "))";
        final String query = "SELECT p FROM Post p WHERE " + HAVERSINE + "  < :radius"; // base query
        int counter = 0;
        try {
            do {
                changed = false;
                radius += 5000.00;
                Map<String, Object> params = new HashMap<>();
                params.put("latitude", latitude);
                params.put("longitude", longitude);
                params.put("radius", radius);
                if("comments".equals(criteria)) {
                    final String query2 = " ORDER BY p.comment_number DESC, p.id DESC";
                    posts = Post.find(query+query2, params).range(0, 50).list();
                } else if ("votes".equals(criteria))  {
                    final String query2 = " ORDER BY p.votingValue DESC, p.id DESC";
                    posts = Post.find(query+query2, params).range(0, 50).list();
                } else {
                    final String query2 = " ORDER BY p.id DESC";
                    posts = Post.find(query+query2, params).range(0, 50).list();
                }
                //(double) radius).range(startIndex, stopIndex).list();
                if (posts.size() > oldSize && counter > 20) {
                    oldSize = posts.size();
                    changed = true;
                }


                for (Post post : posts) {
                    if (post.channel.id.equals(channelid)) {
                        postsFiltered.add(post);
                    }
                }

                for (Post post : postsFiltered) {
                    if (post.user.equals(user)) {
                        post.yours = true;
                    }

                    Date d = new Date();
                    boolean isExpired = (d.getTime() - post.postedAt.getTime()) > 86400000;

                    if(Post.TYPE.SURVEY.equals(post.type)) {
                        post.surveys = SurveyOption.findSurveysOfPostId(post.id, user);
                        SurveyVote vote = SurveyVote.findVote(user, post.id);
                        post.vote_id = vote == null ? null : vote.options_id;
                        post.survey_votes = SurveyVote.findVote(user, post.id) == null && !isExpired? null: post.survey_votes;
                    }

                    for (Voting vote : post.votings) {
                        if (vote.user.equals(user)) {
                            vote.yours = true;
                        }
                    }
                }
                //while autoDistance enabled, postsize is max 50 AND (the posts have increases OR the loop reached 20)
            } while (user.autoDistance && postsFiltered.size() < 50 && (++counter < 20 || changed));


            return postsFiltered;
        } catch (SecurityException | IllegalStateException e) {
            e.printStackTrace();
        }

        return Post.listAll(Sort.descending("id"));
    }

    public Post getPost(Long id, User user) throws HttpExceptions {
        Post p = Post.findById(id);
        if(p != null) {
            final String query = "SELECT c FROM Comment c WHERE post_id = ?1"; // base query
            if(p.user.equals(user)) {
                p.yours = true;
            }
            p.comments = Comment.find(query, p.id).range(0, 50).list();
            for (Comment comment : p.comments) {
                if (comment.user.equals(user)) {
                    comment.yours = true;
                }
                for (Voting vote : comment.votings) {
                    if (vote.user.equals(user)) {
                        vote.yours = true;
                    }
                }
            }

            Date d = new Date();
            boolean isExpired = (d.getTime() - p.postedAt.getTime()) > 86400000;

            if(Post.TYPE.SURVEY.equals(p.type)) {
                p.surveys = SurveyOption.findSurveysOfPostId(p.id, user);
                SurveyVote vote = SurveyVote.findVote(user, p.id);
                p.vote_id = vote == null ? null : vote.options_id;
                p.survey_votes = SurveyVote.findVote(user, p.id) == null && !isExpired? null: p.survey_votes;
            }

            for (Voting vote : p.votings) {
                if (vote.user.equals(user)) {
                    vote.yours = true;
                }
            }
            return p;
        } else {
            throw new HttpExceptions(null, Response.Status.NOT_FOUND);
        }
    }




    public List<Post> getMine(String by, User user) {
        List<Post> posts = Post.listAll();
        List<Post> postsFiltered = new ArrayList<> ();

        if("post".equals(by)) {
            for (Post post : posts) {
                if (post.user.id.equals(user.id)) {
                    postsFiltered.add(post);
                }
            }
        } else if("comment".equals(by)) {
            for (Post post : posts) {
                final String query = "SELECT c FROM Comment c WHERE post_id = ?1";
                post.comments = Comment.find(query, post.id).list();
                for (Comment comment : post.comments) {
                    if (comment.user.equals(user)) {
                        postsFiltered.add(post);
                        break;
                    }
                }
            }
        } else if("vote".equals(by)) {
            for (Post post : posts) {
                boolean add = false;

                for (Voting vote : post.votings) {
                    if (vote.user.equals(user)) {
                        vote.yours = true;
                        add = true;
                        break;
                    }
                }

                final String query = "SELECT c FROM Comment c WHERE post_id = ?1";
                post.comments = Comment.find(query, post.id).list();
                for (Comment comment : post.comments) {
                    if (comment.user.equals(user)) {
                        comment.yours = true;
                        for (Voting vote : comment.votings) {
                            if (vote.user.equals(user)) {
                                add = true;
                                break;
                            }
                        }
                    }
                }
                if(add) {
                    postsFiltered.add(post);
                }
            }
        } else {
            return postsFiltered;
        }


        for (Post post : postsFiltered) {
            if (post.user.equals(user)) {
                post.yours = true;
            }

            Date d = new Date();
            boolean isExpired = (d.getTime() - post.postedAt.getTime()) > 86400000;

            if(Post.TYPE.SURVEY.equals(post.type)) {
                post.surveys = SurveyOption.findSurveysOfPostId(post.id, user);
                SurveyVote vote = SurveyVote.findVote(user, post.id);
                post.vote_id = vote == null ? null : vote.options_id;
                post.survey_votes = SurveyVote.findVote(user, post.id) == null && !isExpired? null: post.survey_votes;
            }

            for (Voting vote : post.votings) {
                if (vote.user.equals(user)) {
                    vote.yours = true;
                }
            }
        }
        return postsFiltered;
    }

    public List<Post> getPosts(User user) {
        return Post.findByUser(user);
    }

    /**
     * Creates post
     *
     * @param user of the post
     * @return created post
     * @throws HttpExceptions exception
     */
    @Transactional
    public Post createPost(Channel channel, String hashtag, String text, Double longitude, Double latitude, String city, String color, User user) throws HttpExceptions {
       try {
            Post post = new Post();
            post.text = text;
            post.channel = channel;
            post.hashtag = hashtag;
            post.longitude = longitude;
            post.latitude = latitude;
            post.city = city;
            post.user = user;
            post.color = color;
            Calendar calendar = Calendar.getInstance();
            java.util.Date currentDate = calendar.getTime();
            post.postedAt = new Date(currentDate.getTime());
            post.votingValue = 0;
            post.survey_votes = 0;
            post.image = null;
            post.persistAndFlush();

            return post;
        } catch (PersistenceException exception) {
            LOGGER.error("Post creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Post already exists", Response.Status.CONFLICT);
        }
    }


    /**
     * Creates post
     *
     * @param user of the post
     * @return created post
     * @throws HttpExceptions exception
     */
    @Transactional
    public Post createLinkPost(Channel channel, String link, String hashtag,String text, Double longitude, Double latitude, String city, String color, User user) throws HttpExceptions {
        try {
            Post post = new Post();
            post.type = Post.TYPE.LINK;
            post.text = text;
            post.channel = channel;
            post.hashtag = hashtag;
            post.longitude = longitude;
            post.latitude = latitude;
            post.city = city;
            post.user = user;
            post.color = color;
            Calendar calendar = Calendar.getInstance();
            java.util.Date currentDate = calendar.getTime();
            post.postedAt = new Date(currentDate.getTime());
            post.votingValue = 0;
            post.survey_votes = 0;
            post.image = null;
            post.link = link;
            String pattern = "(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?$";
            if(!Pattern.matches(pattern, link)) {
                throw new HttpExceptions("No link", Response.Status.CONFLICT);
            }

            post.persistAndFlush();

            return post;
        } catch (PersistenceException exception) {
            LOGGER.error("Post creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Post already exists", Response.Status.CONFLICT);
        }
    }

    /**
     * Creates post
     *
     * @param imgbase64
     * @param user of the post
     * @return created post
     * @throws HttpExceptions exception
     */
    @Transactional
    public Post createImagePost(Channel channel, String imgbase64, Double longitude, Double latitude, String city, String color, User user) throws HttpExceptions {
        try {
            Post post = new Post();
            post.type = Post.TYPE.IMAGE;
            post.channel = channel;
            post.text = null;
            post.hashtag = null;
            post.longitude = longitude;
            post.latitude = latitude;
            post.city = city;
            post.user = user;
            post.color = color;
            Calendar calendar = Calendar.getInstance();
            java.util.Date currentDate = calendar.getTime();
            post.postedAt = new Date(currentDate.getTime());
            post.votingValue = 0;
            post.survey_votes = 0;
            post.image = null;
            try {
                String[] encoded = imgbase64.split(",");
                byte [] barr = Base64.getDecoder().decode(encoded[1]);
                post.image = barr;
            } catch (Exception e){
                e.printStackTrace();
                throw new HttpExceptions("NOT AN IMAGE", Response.Status.CONFLICT);
            }

            post.persistAndFlush();

            return post;
        } catch (PersistenceException exception) {
            LOGGER.error("Post creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Post already exists", Response.Status.CONFLICT);
        }
    }


    /**
     * Creates post
     *
     * @param user of the post
     * @return created post
     * @throws HttpExceptions exception
     */
    @Transactional
    public Post createSurveyPost(Channel channel, String hashtag,String text, Double longitude, Double latitude, String city, String color, User user) throws HttpExceptions {
        try {
            Post post = new Post();
            post.type = Post.TYPE.SURVEY;
            post.text = text;
            post.channel = channel;
            post.hashtag = hashtag;
            post.longitude = longitude;
            post.latitude = latitude;
            post.city = city;
            post.user = user;
            post.color = color;
            Calendar calendar = Calendar.getInstance();
            java.util.Date currentDate = calendar.getTime();
            post.postedAt = new Date(currentDate.getTime());
            post.votingValue = 0;
            post.survey_votes = 0;
            post.image = null;

            post.persistAndFlush();

            return post;
        } catch (PersistenceException exception) {
            LOGGER.error("Post creation");
            LOGGER.error(exception.getCause());
            LOGGER.error(exception.getMessage());
            throw new HttpExceptions("Post already exists", Response.Status.CONFLICT);
        }
    }

    /**
     * Deletes Post by given id
     * @param id postId
     * @return Post
     */
    @Transactional
    public Post deletePost(Long id) {
        Post post = Post.findById(id);
        if(post != null) {
            try {
                Comment.delete("post_id = " +post.id);
                em.remove(post);
            } catch (PersistenceException exception) {
                LOGGER.error("Post remove");
                LOGGER.error(exception.getCause());
                LOGGER.error(exception.getMessage());
            }
        }
        return null;
    }

    /**
     * Updates comment number
     * @param id of the comment
     * @param value the value
     * @return true if no errors, else false
     */
    @Transactional
    public boolean updateCommentNumber(Long id, int value) {
        Post post = Post.findById(id);
        if(post != null) {
            try {
                post.comment_number += value;
                post.persistAndFlush();
                return true;
            } catch (Exception exception) {
                LOGGER.error("Post Update Comment");
                LOGGER.error(exception.getCause());
                LOGGER.error(exception.getMessage());
            }
        }
        return false;
    }

    /**
     * Updates voting
     * @param id of the post
     * @param t Voting.TYPE
     * @param value the value
     * @return true if no errors, else false
     */
    @Transactional
    public boolean updateVoting(Long id, Voting.TYPE t, int value) {
        Post post = Post.findById(id);
        if(post != null) {
            try {
                post.votingValue += value;
                post.persistAndFlush();
                return true;
            } catch (Exception exception) {
                LOGGER.error("Post Update Comment");
                LOGGER.error(exception.getCause());
                LOGGER.error(exception.getMessage());
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    @Transactional
    public boolean updateSurveyVotingNumber(Long postId) {
        Post post = Post.findById(postId);
        if(post != null) {
            try {
                post.survey_votes ++;
                post.persistAndFlush();
                return true;
            } catch (Exception exception) {
                LOGGER.error("Post Update Comment");
                LOGGER.error(exception.getCause());
                LOGGER.error(exception.getMessage());
            }
        }
        return false;
    }

    /**
     * Removes Post by given id
     * @param id postId
     * @return true if no errors, else false
     */
    @Transactional
    public boolean removePost(Long id) {
        return Post.deleteById(id);
    }

    /**
     * Removes all posts
     */
    @Transactional
    public void removeAllPosts() {
        try {
            Query del = em.createQuery("DELETE FROM Post p");
            del.executeUpdate();

        } catch (SecurityException | IllegalStateException e) {
            e.printStackTrace();
        }
    }
}