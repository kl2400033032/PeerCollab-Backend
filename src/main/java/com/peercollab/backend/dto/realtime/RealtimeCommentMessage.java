package com.peercollab.backend.dto.realtime;

import com.peercollab.backend.dto.comment.CommentResponse;

public record RealtimeCommentMessage(
        String event,
        Long projectId,
        CommentResponse comment
) {
}
