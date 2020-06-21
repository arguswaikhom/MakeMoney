package com.squadx.crown.makemoneyapp.controller;

import android.content.Context;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squadx.crown.makemoneyapp.R;
import com.squadx.crown.makemoneyapp.model.User;
import com.squadx.crown.makemoneyapp.util.FirestoreSync;

import java.util.HashMap;
import java.util.Map;

public class ReactionController {
    public static final int ACTION_UP_VOTE = 0;
    public static final int ACTION_DOWN_VOTE = 1;
    public static final int CRUD_ADD = 2;
    public static final int CRUD_REMOVE = 3;
    public static final int CRUD_UPDATE = 4;

    public static void clicked(Context context, String article, int action) {
        context = context.getApplicationContext();
        PreferenceController pre = PreferenceController.getInstance(context);
        Map<String, Boolean> reactions = pre.getReactions();
        if (reactions.containsKey(article)) {
            Boolean value = reactions.get(article);
            if (value == null) {
                addToReaction(reactions, article, action);
                updateFirestore(context, article, action, CRUD_ADD);
            } else if (value) {
                if (action == ACTION_UP_VOTE) {
                    reactions.remove(article);
                    updateFirestore(context, article, action, CRUD_REMOVE);
                } else if (action == ACTION_DOWN_VOTE) {
                    reactions.put(article, false);
                    updateFirestore(context, article, action, CRUD_UPDATE);
                }
            } else {
                if (action == ACTION_UP_VOTE) {
                    reactions.put(article, true);
                    updateFirestore(context, article, action, CRUD_UPDATE);
                } else if (action == ACTION_DOWN_VOTE) {
                    reactions.remove(article);
                    updateFirestore(context, article, action, CRUD_REMOVE);
                }
            }
        } else {
            addToReaction(reactions, article, action);
            updateFirestore(context, article, action, CRUD_ADD);
        }
        pre.updateReactions(reactions);
    }

    private static void addToReaction(Map<String, Boolean> reactions, String article, int action) {
        if (action == ACTION_UP_VOTE) reactions.put(article, true);
        else if (action == ACTION_DOWN_VOTE) reactions.put(article, false);
    }

    private static void updateFirestore(Context context, String article, int action, int crud) {
        if (!AppController.getInstance().isAuthenticated()) return;
        User user = PreferenceController.getInstance(context).getUser();
        switch (crud) {
            case CRUD_ADD: {
                if (action == ACTION_UP_VOTE) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("upVotes", FieldValue.increment(1));
                    updateOnArticle(context, article, param);
                    updateOnReaction(context, article, user.getUserId(), true);
                } else if (action == ACTION_DOWN_VOTE) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("downVotes", FieldValue.increment(1));
                    updateOnArticle(context, article, param);
                    updateOnReaction(context, article, user.getUserId(), false);
                }
                break;
            }
            case CRUD_REMOVE: {
                if (action == ACTION_UP_VOTE) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("upVotes", FieldValue.increment(-1));
                    updateOnArticle(context, article, param);
                    deleteReaction(context, article, user.getUserId());
                } else if (action == ACTION_DOWN_VOTE) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("downVotes", FieldValue.increment(-1));
                    updateOnArticle(context, article, param);
                    deleteReaction(context, article, user.getUserId());
                }
                break;
            }
            case CRUD_UPDATE: {
                if (action == ACTION_UP_VOTE) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("upVotes", FieldValue.increment(1));
                    param.put("downVotes", FieldValue.increment(-1));
                    updateOnArticle(context, article, param);
                    updateOnReaction(context, article, user.getUserId(), true);
                } else if (action == ACTION_DOWN_VOTE) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("downVotes", FieldValue.increment(1));
                    param.put("upVotes", FieldValue.increment(-1));
                    updateOnArticle(context, article, param);
                    updateOnReaction(context, article, user.getUserId(), false);
                }
                break;
            }
        }
    }

    private static void updateOnArticle(Context context, String article, Map<String, Object> param) {
        FirebaseFirestore.getInstance().collection(context.getString(R.string.col_article))
                .document(article).update(param);
    }

    private static void updateOnReaction(Context context, String article, String userId, boolean action) {
        Map<String, Object> param = new HashMap<>();
        param.put(context.getString(R.string.field_action), action);
        FirebaseFirestore.getInstance().collection(context.getString(R.string.col_reaction)).document(userId)
                .collection(context.getString(R.string.col_article)).document(article).set(param);
    }

    private static void deleteReaction(Context context, String article, String userId) {
        FirebaseFirestore.getInstance().collection(context.getString(R.string.col_reaction)).document(userId)
                .collection(context.getString(R.string.col_article)).document(article).delete();
    }

    public static void syncWithFirestore(Context context, FirestoreSync callback) {
        if (!AppController.getInstance().isAuthenticated()) return;
        User user = PreferenceController.getInstance(context.getApplicationContext()).getUser();
        FirebaseFirestore.getInstance().collection(context.getString(R.string.col_reaction)).document(user.getUserId())
                .collection(context.getString(R.string.col_article)).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        Map<String, Boolean> ur = new HashMap<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            if (doc.exists()) {
                                Boolean action = (Boolean) doc.get("action");
                                if (action != null) ur.put(doc.getId(), action);
                            }
                        }

                        PreferenceController.getInstance(context.getApplicationContext()).updateReactions(ur);
                        if (callback != null) callback.onCompleted();
                    }
                });
    }
}