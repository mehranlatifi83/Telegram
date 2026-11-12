/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.telegram.ui.Cells;

import org.telegram.tgnet.ConnectionsManager;
import org.telegram.ui.Components.Premium.boosts.cells.msg.GiveawayAccessibilityCard;
import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.formatPluralString;
import static org.telegram.messenger.LocaleController.formatString;
import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.messenger.MessageObject.getMedia;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.utils.code.highlight.CodeHighlighting;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.RichMessageLayout;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.MediaActionDrawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

class ChatMessageAccessibilityNodeProvider extends AccessibilityNodeProvider {

    private final ChatMessageCell cell;

    ChatMessageAccessibilityNodeProvider(ChatMessageCell cell) {
        this.cell = cell;
    }

    public static final int RICH_MEDIA_START = 6000;
    public static final int RECOMMENDATIONS_START = 7000;
    public static final int PROFILE = 5000;
    public static final int LINK_IDS_START = 2000;
    public static final int LINK_CAPTION_IDS_START = 3000;
    public static final int BOT_BUTTONS_START = 1000;
    public static final int POLL_BUTTONS_START = 500;
    public static final int INSTANT_VIEW = 499;
    public static final int SHARE = 498;
    public static final int REPLY = 497;
    public static final int COMMENT = 496;
    public static final int POLL_HINT = 495;
    public static final int FORWARD = 494;
    public static final int TRANSCRIBE = 493;
    public static final int CONTACT = 492;
    public static final int CONTACT_VIEW = 491;
    public static final int CONTACT_ADD = 490;
    public static final int CONTACT_MESSAGE = 489;
    public static final int POLL_ADD_OPTION = 488;
    // well clear of the ids that stand for one button each, which run down from 499: a range
    // that reached up into them would answer for every one of them and hide the lot
    public static final int GIVEAWAY_BUTTONS_START = 300;
    public static final int GIVEAWAY_BUTTONS_END = 400;
    public static final int SHOW_MORE = 487;
    private Path linkPath = new Path();
    private RectF rectF = new RectF();
    private Rect rect = new Rect();

    private class ProfileSpan extends ClickableSpan {
        private TLRPC.User user;

        public ProfileSpan(TLRPC.User user) {
            this.user = user;
        }

        @Override
        public void onClick(@NonNull View view) {
            if (cell.delegate != null) {
                cell.delegate.didPressUserAvatar(cell, user, 0, 0, false);
            }
        }
    }

    private RichMessageLayout.RichBlock resolveRichElement(int virtualViewId, int[] outLocal) {
        if (cell.currentMessageObject == null || cell.currentMessageObject.richLayout == null) {
            return null;
        }
        final int globalIndex = virtualViewId - RICH_MEDIA_START;
        if (globalIndex < 0) {
            return null;
        }
        final RichMessageLayout richLayout = cell.currentMessageObject.richLayout;
        int acc = 0;
        for (int b = 0; b < richLayout.blocks.size(); b++) {
            final RichMessageLayout.RichBlock block = richLayout.blocks.get(b);
            if (!block.isVisible()) {
                continue;
            }
            final int count = block.getAccessibilityElementCount();
            if (globalIndex < acc + count) {
                outLocal[0] = globalIndex - acc;
                return block;
            }
            acc += count;
        }
        return null;
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo(int virtualViewId) {
        int[] pos = {0, 0};
        cell.getLocationOnScreen(pos);
        if (virtualViewId == HOST_VIEW_ID) {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain(cell);
            cell.onInitializeAccessibilityNodeInfo(info);
            final boolean unread = cell.currentMessageObject != null && cell.currentMessageObject.isOut() && !cell.currentMessageObject.scheduled && cell.currentMessageObject.isUnread();
            final boolean contentUnread = cell.currentMessageObject != null && cell.currentMessageObject.isContentUnread();
            final long fileSize = cell.currentMessageObject != null ? cell.currentMessageObject.loadedFileSize : 0;
            final boolean mediaDownloaded = cell.isMediaDownloadedForAccessibility();
            if (cell.accessibilityText == null || cell.accessibilityTextUnread != unread || cell.accessibilityTextContentUnread != contentUnread || cell.accessibilityTextFileSize != fileSize || cell.accessibilityTextButtonState != cell.buttonState || cell.accessibilityTextMiniButtonState != cell.miniButtonState || cell.accessibilityTextMediaDownloaded != mediaDownloaded) {
                SpannableStringBuilder sb = new SpannableStringBuilder();
                // what kind of message this is, when a block of it says (a widget), before anything else
                final CharSequence kind = cell.currentMessageObject.richLayout != null ? cell.currentMessageObject.richLayout.getAccessibilityKind() : null;
                if (!TextUtils.isEmpty(kind)) {
                    sb.append(kind);
                    sb.append(", ");
                }
                // the pin by the time of a pinned message is drawn, and was not said: it comes
                // first, so it is heard before the rest of the message
                if (cell.isPinned) {
                    sb.append(getString(R.string.PinnedMessage));
                    sb.append(", ");
                }
                if (cell.isNeedAccessibilityAuthorName()) {
                    sb.append(cell.getAuthorName());
                    if (cell.currentUser != null) {
                        sb.setSpan(new ProfileSpan(cell.currentUser), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    final CharSequence adminText = cell.getAdminAccessibilityText();
                    if (!TextUtils.isEmpty(adminText)) {
                        if (cell.adminLayoutIsTag) {
                            sb.append(' ').append(formatString(cell.adminLayoutIsAdmin ? R.string.AccDescrWithAdminTag : R.string.AccDescrWithMemberTag, adminText));
                        } else {
                            sb.append(", ").append(adminText);
                        }
                    }
                    sb.append('\n');
                }
                // the bot is drawn on the line of the sender's name, after it, or alone where
                // no name is drawn
                if (!TextUtils.isEmpty(cell.accessibilityViaBotText)) {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                        sb.replace(sb.length() - 1, sb.length(), " ");
                    }
                    sb.append(cell.accessibilityViaBotText);
                    sb.append('\n');
                }
                if (cell.drawForwardedName) {
                    for (int a = 0; a < 2; a++) {
                        if (cell.forwardedNameLayout[a] != null && cell.forwardedNameLayout[a].getText() != null) {
                            sb.append(cell.forwardedNameLayout[a].getText());
                            sb.append(a == 0 ? " " : "\n");
                        }
                    }
                }
                if (cell.documentAttach != null && cell.documentAttachType == ChatMessageCell.DOCUMENT_ATTACH_TYPE_DOCUMENT) {
                    String fileName = FileLoader.getAttachFileName(cell.documentAttach);
                    if (fileName.indexOf('.') != -1) {
                        sb.append(formatString(R.string.AccDescrDocumentType, fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase(Locale.ROOT)));
                    }
                }
                // a message of an album is one of several, each drawn in a cell of its own, and
                // nothing said which of them was being read or how many there were: going
                // through an album was a run of messages that all sounded the same
                final CharSequence albumPlace = cell.albumAccessibilityPlace();
                if (albumPlace != null) {
                    sb.append(albumPlace);
                    sb.append(", ");
                }
                if (cell.currentMessageObject.richLayout != null && !cell.currentMessageObject.richLayout.blocks.isEmpty()) {
                    for (RichMessageLayout.RichBlock block : cell.currentMessageObject.richLayout.blocks) {
                        if (!block.isVisible()) {
                            continue;
                        }
                        final int before = sb.length();
                        final SpannableStringBuilder blockText = new SpannableStringBuilder();
                        block.appendAccessibilityText(blockText);
                        final CharSequence blockLabel = block.getAccessibilityLabel();
                        final CharSequence listMarker = block.getAccessibilityListMarker();
                        if (!TextUtils.isEmpty(listMarker)) {
                            sb.append(listMarker);
                            if (!TextUtils.isEmpty(blockLabel) || blockText.length() > 0) {
                                sb.append(' ');
                            }
                        }
                        if (!TextUtils.isEmpty(blockLabel)) {
                            sb.append(blockLabel);
                            if (blockText.length() > 0) {
                                sb.append(", ");
                            }
                        }
                        sb.append(blockText);
                        if (sb.length() > before && sb.charAt(sb.length() - 1) != '\n') {
                            sb.append('\n');
                        }
                    }
                } else if (!TextUtils.isEmpty(cell.currentMessageObject.messageText)) {
                    CharSequence messageText = cell.currentMessageObject.messageText;
                    // translating a chat puts the translated caption in the message text of a
                    // media message, where its type belongs: the caption is read on its own
                    // below, so report the type here as an untranslated message does
                    if (!cell.currentMessageObject.isMediaEmpty() && !TextUtils.isEmpty(cell.currentMessageObject.caption) && TextUtils.equals(messageText, cell.currentMessageObject.caption)) {
                        messageText = cell.currentMessageObject.getMediaTitle(MessageObject.getMedia(cell.currentMessageObject.messageOwner));
                    }
                    if (messageText instanceof Spanned) {
                        final Spanned spanned = (Spanned) messageText;
                        CodeHighlighting.Span[] codeSpans = spanned.getSpans(0, spanned.length(), CodeHighlighting.Span.class);
                        if (codeSpans != null && codeSpans.length > 0) {
                            SpannableStringBuilder ssb = new SpannableStringBuilder(messageText);
                            Arrays.sort(codeSpans, (a, b) -> spanned.getSpanStart(b) - spanned.getSpanStart(a));
                            for (CodeHighlighting.Span span : codeSpans) {
                                int start = spanned.getSpanStart(span);
                                if (start < 0) {
                                    continue;
                                }
                                final CharSequence label = TextUtils.isEmpty(span.lng)
                                    ? getString(R.string.AccDescrCodeBlock)
                                    : formatString(R.string.AccDescrCodeBlockLanguage, MessageObject.TextLayoutBlock.capitalizeLanguage(span.lng));
                                ssb.insert(start, label + ". ");
                            }
                            messageText = ssb;
                        }
                    }
                    final CharSequence mediaTitle = cell.currentMessageObject.getMediaTitle(MessageObject.getMedia(cell.currentMessageObject.messageOwner));
                    // a message of an album names itself by its kind, and the album line just
                    // ahead of it has said that kind already. Leave out only what was said
                    // there: the word album itself, or the kind. A file names itself by its
                    // file name, which is neither of those and is the only place it is said
                    final boolean namedByAlbum = albumPlace != null
                        && (TextUtils.equals(messageText, getString(R.string.Album))
                            || TextUtils.equals(messageText, cell.albumAccessibilityKind()));
                    // a poll names itself twice over: once as the kind of message it is, and
                    // again under its question, where the kind is said in full
                    final boolean namedByPoll = cell.currentMessageObject.isPoll() && cell.lastPoll != null
                        && TextUtils.equals(messageText, mediaTitle);
                    if (!TextUtils.isEmpty(messageText) && !namedByAlbum && !namedByPoll) {
                        sb.append(messageText);
                    }
                    // a game emoji is thrown and lands on something, and what it landed on is
                    // in the message from the moment it arrives: the animation only plays it
                    // out. None of it was said, so a screen reader was told a game had been
                    // sent and never how it went
                    final CharSequence outcome = cell.diceAccessibilityOutcome();
                    if (!TextUtils.isEmpty(outcome)) {
                        sb.append(", ");
                        sb.append(outcome);
                    }
                    // a giveaway is a card drawn by hand: the prize, who can take part, the
                    // chats it runs in, the countries it is open to and the day the winners
                    // are picked. None of it is text of the message, so none of it was said
                    // and the message was as good as empty
                    final GiveawayAccessibilityCard giveawayCard = cell.giveawayAccessibilityCard();
                    final CharSequence giveaway = giveawayCard == null ? null : giveawayCard.getAccessibilityText();
                    if (!TextUtils.isEmpty(giveaway)) {
                        sb.append(", ");
                        sb.append(giveaway);
                    }
                }
                // an effect is chosen when a message is sent and drawn on it ever after: it is
                // as much a part of what was sent as the words are, and it was never said
                final TLRPC.TL_availableEffect effect = cell.getEffect();
                if (effect != null && !TextUtils.isEmpty(effect.emoticon)) {
                    sb.append(", ");
                    sb.append(formatString(R.string.AccDescrMessageEffect, effect.emoticon));
                }
                cell.accessibilityTextTransferIndex = sb.length();
                if (cell.currentMessageObject.isMusic()) {
                    sb.append("\n");
                    sb.append(formatString("AccDescrMusicInfo", R.string.AccDescrMusicInfo, cell.currentMessageObject.getMusicAuthor(), cell.currentMessageObject.getMusicTitle()));
                    sb.append(", ");
                    sb.append(LocaleController.formatDuration((int) cell.currentMessageObject.getDuration()));
                } else if (cell.currentMessageObject.isVoice() || cell.isRoundVideo) {
                    sb.append(", ");
                    sb.append(LocaleController.formatDuration((int) cell.currentMessageObject.getDuration()));
                    sb.append(", ");
                    if (cell.currentMessageObject.isContentUnread()) {
                        sb.append(getString("AccDescrMsgNotPlayed", R.string.AccDescrMsgNotPlayed));
                    } else {
                        sb.append(getString("AccDescrMsgPlayed", R.string.AccDescrMsgPlayed));
                    }
                }
                if (cell.lastPoll != null && cell.currentMessageObject.isPoll()) {
                    // the poll of a previous message stays around on a recycled cell, so it is
                    // only of this message when this message is a poll itself, and the kind of
                    // poll comes before the question, the way it is drawn: the line above the
                    // question is what says a poll is a poll
                    String title;
                    if (cell.pollClosed) {
                        title = getString("FinalResults", R.string.FinalResults);
                    } else {
                        if (cell.lastPoll.quiz) {
                            if (cell.lastPoll.public_voters) {
                                title = getString("QuizPoll", R.string.QuizPoll);
                            } else {
                                title = getString("AnonymousQuizPoll", R.string.AnonymousQuizPoll);
                            }
                        } else if (cell.lastPoll.public_voters) {
                            title = getString("PublicPoll", R.string.PublicPoll);
                        } else {
                            title = getString("AnonymousPoll", R.string.AnonymousPoll);
                        }
                    }
                    sb.append(", ");
                    sb.append(title);
                    sb.append(", ");
                    sb.append(cell.lastPoll.question.text);
                    final CharSequence recentVoters = cell.pollRecentVoterNames(cell.poll.lastResultsObj == null ? null : cell.poll.lastResultsObj.recent_voters);
                    if (recentVoters != null) {
                        sb.append(", ");
                        sb.append(recentVoters);
                    }
                    // the counter under a poll, and the state the poll is in. All of it is
                    // written on the card, so a sighted reader has it without touching
                    // anything, while nothing here was ever said out loud
                    sb.append(", ");
                    if (cell.lastPollResultsVoters == 0) {
                        sb.append(getString(cell.lastPoll.quiz ? R.string.NoVotesQuiz : R.string.NoVotes));
                    } else {
                        sb.append(formatPluralString(cell.lastPoll.quiz ? "Answer" : "Vote", cell.lastPollResultsVoters));
                    }
                    if (cell.lastPoll.multiple_choice && !cell.pollVoted && !cell.pollClosed) {
                        sb.append(", ");
                        sb.append(getString(R.string.AccDescrPollMultipleChoice));
                    }
                    if (cell.pollVoted) {
                        sb.append(", ");
                        sb.append(getString(R.string.AccDescrPollVoted));
                    }
                    if (!cell.pollClosed) {
                        if (cell.lastPoll.hide_results_until_close && !cell.lastPoll.creator) {
                            sb.append(", ");
                            sb.append(getString(R.string.PollResultsWillLater));
                        }
                        final int closeDate = cell.lastPoll.close_date;
                        if (closeDate > 0 && closeDate > ConnectionsManager.getInstance(cell.currentAccount).getCurrentTime()) {
                            // the card counts down by the second. Saying when the poll closes
                            // instead of how long is left keeps this text still, so the message
                            // is not reported as changed over and over
                            sb.append(", ");
                            sb.append(formatString(R.string.AccDescrPollClosesAt, LocaleController.formatDateTime(closeDate, true)));
                        }
                    }
                    // a poll can carry a picture, a sticker, a place, a piece of music or a
                    // file of its own, and its explanation can carry another. Both are drawn
                    // on the card and neither was ever spoken
                    if (cell.hasPollDescriptionMedia()) {
                        sb.append(", ");
                        sb.append(cell.pollMediaDescription(cell.poll.contentDrawable.getMedia()));
                    }
                    if (cell.hasPollExplanationMedia()) {
                        sb.append(", ");
                        sb.append(formatString(R.string.AccDescrPollExplanationMedia, cell.pollMediaDescription(cell.poll.explanationDrawable.getMedia())));
                    }
                    // the explanation of a quiz is drawn under the poll once its button has
                    // been pressed. It was never spoken: the button could be pressed, and
                    // what it opened stayed unread
                    if (cell.currentMessageObject.expandedExplanation && !TextUtils.isEmpty(cell.poll.currentExplanation)) {
                        sb.append("\n");
                        sb.append(getString(R.string.QuizExplanationTitle));
                        sb.append(", ");
                        sb.append(cell.poll.currentExplanation);
                    }
                }
                // a checklist is drawn with the same two lines a poll is: what kind of list it
                // is, under the title, and how much of it is done, under the items. Neither was
                // ever spoken, because the block above is only entered for a poll, so all a
                // checklist said was its title
                if (cell.currentMessageObject.isTodo()) {
                    if (cell.docTitleLayout != null && !TextUtils.isEmpty(cell.docTitleLayout.getText())) {
                        sb.append(", ");
                        sb.append(cell.docTitleLayout.getText());
                    }
                    if (cell.animatedInfoLayout != null && !TextUtils.isEmpty(cell.animatedInfoLayout.getText())) {
                        sb.append(", ");
                        sb.append(cell.animatedInfoLayout.getText());
                    }
                }
                if (cell.documentAttach != null) {
                    if (cell.documentAttachType == ChatMessageCell.DOCUMENT_ATTACH_TYPE_VIDEO) {
                        sb.append(", ");
                        sb.append(LocaleController.formatDuration((int) cell.currentMessageObject.getDuration()));
                    }
                    if (cell.buttonState == 0 || cell.documentAttachType == ChatMessageCell.DOCUMENT_ATTACH_TYPE_DOCUMENT) {
                        sb.append(", ");
                        sb.append(AndroidUtilities.formatFileSize(cell.documentAttach.size));
                    }
                }
                // whether what the message holds is already on the phone is drawn as the
                // button over it and was never said, though it is what decides what pressing
                // the message does. It comes after the length and the size, where the button
                // is drawn
                if (cell.hasAccessibilityDownloadState()) {
                    sb.append(", ");
                    sb.append(getString(mediaDownloaded ? R.string.AccDescrMediaDownloaded : R.string.AccDescrMediaNotDownloaded));
                }
                if (cell.currentMessageObject.isVoiceTranscriptionOpen()) {
                    sb.append("\n");
                    sb.append(cell.currentMessageObject.getVoiceTranscription());
                } else {
                    if (MessageObject.getMedia(cell.currentMessageObject.messageOwner) != null && !TextUtils.isEmpty(cell.currentMessageObject.caption)) {
                        sb.append("\n");
                        sb.append(cell.currentMessageObject.caption);
                    }
                }
                if (cell.currentMessageObject.isOut()) {
                    if (cell.currentMessageObject.isSent()) {
                        sb.append("\n");
                        if (cell.currentMessageObject.scheduled) {
                            sb.append(formatString("AccDescrScheduledDate", R.string.AccDescrScheduledDate, cell.currentTimeString));
                        } else {
                            sb.append(formatString("AccDescrSentDate", R.string.AccDescrSentDate, getString("TodayAt", R.string.TodayAt) + " " + cell.currentTimeString));
                            sb.append(", ");
                            sb.append(cell.currentMessageObject.isUnread() ? getString("AccDescrMsgUnread", R.string.AccDescrMsgUnread) : getString("AccDescrMsgRead", R.string.AccDescrMsgRead));
                        }
                    } else if (cell.currentMessageObject.isSending()) {
                        if (cell.getTransferPercent() < 0) {
                            sb.append("\n");
                            sb.append(getString("AccDescrMsgSending", R.string.AccDescrMsgSending));
                            final float sendingProgress = cell.radialProgress.getProgress();
                            if (sendingProgress > 0f) {
                                sb.append(" ").append(Integer.toString(Math.round(sendingProgress * 100))).append("%");
                            }
                        }
                    } else if (cell.currentMessageObject.isSendError()) {
                        sb.append("\n");
                        sb.append(getString("AccDescrMsgSendingError", R.string.AccDescrMsgSendingError));
                    }
                } else {
                    sb.append("\n");
                    sb.append(formatString("AccDescrReceivedDate", R.string.AccDescrReceivedDate, getString("TodayAt", R.string.TodayAt) + " " + cell.currentTimeString));
                }
                if (cell.getRepliesCount() > 0 && !cell.hasCommentLayout()) {
                    sb.append("\n");
                    sb.append(formatPluralString("AccDescrNumberOfReplies", cell.getRepliesCount()));
                }
                if (cell.currentMessageObject.messageOwner.reactions != null && cell.currentMessageObject.messageOwner.reactions.results != null) {
                    if (cell.currentMessageObject.messageOwner.reactions.results.size() == 1) {
                        TLRPC.ReactionCount reaction = cell.currentMessageObject.messageOwner.reactions.results.get(0);
                        CharSequence emoticon = MessageObject.describeReaction(cell.currentAccount, reaction.reaction);
                        if (reaction.count == 1) {
                            sb.append("\n");
                            boolean isMe = false;
                            String userName = "";
                            if (cell.currentMessageObject.messageOwner.reactions.recent_reactions != null && cell.currentMessageObject.messageOwner.reactions.recent_reactions.size() == 1) {
                                TLRPC.MessagePeerReaction recentReaction = cell.currentMessageObject.messageOwner.reactions.recent_reactions.get(0);
                                if (recentReaction != null) {
                                    TLRPC.User user = MessagesController.getInstance(cell.currentAccount).getUser(MessageObject.getPeerId(recentReaction.peer_id));
                                    isMe = UserObject.isUserSelf(user);
                                    if (user != null) {
                                        userName = UserObject.getFirstName(user);
                                    }
                                }
                            }
                            if (isMe) {
                                sb.append(formatString("AccDescrYouReactedWith", R.string.AccDescrYouReactedWith, emoticon));
                            } else {
                                sb.append(formatString("AccDescrReactedWith", R.string.AccDescrReactedWith, userName, emoticon));
                            }
                        } else if (reaction.count > 1) {
                            sb.append("\n");
                            sb.append(formatPluralString("AccDescrNumberOfPeopleReactions", reaction.count, emoticon));
                        }
                    } else {
                        sb.append(getString("Reactions", R.string.Reactions)).append((": "));
                        final int count = cell.currentMessageObject.messageOwner.reactions.results.size();
                        for (int i = 0; i < count; ++i) {
                            TLRPC.ReactionCount reactionCount = cell.currentMessageObject.messageOwner.reactions.results.get(i);
                            CharSequence emoticon = MessageObject.describeReaction(cell.currentAccount, reactionCount.reaction);
                            if (reactionCount != null) {
                                sb.append(emoticon).append(" ").append(reactionCount.count + "");
                                if (i + 1 < count) {
                                    sb.append(", ");
                                }
                            }
                        }
                        sb.append("\n");
                    }
                }
                if ((cell.currentMessageObject.messageOwner.flags & TLRPC.MESSAGE_FLAG_HAS_VIEWS) != 0) {
                    sb.append("\n");
                    sb.append(formatPluralString("AccDescrNumberOfViews", cell.currentMessageObject.messageOwner.views));
                }
                sb.append("\n");

                CharacterStyle[] links = sb.getSpans(0, sb.length(), ClickableSpan.class);

                for (CharacterStyle link : links) {
                    int start = sb.getSpanStart(link);
                    int end = sb.getSpanEnd(link);
                    sb.removeSpan(link);

                    ClickableSpan underlineSpan = new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            if (link instanceof ProfileSpan) {
                                ((ProfileSpan) link).onClick(view);
                            } else if (cell.delegate != null) {
                                cell.delegate.didPressUrl(cell, link, false);
                            }
                        }
                    };
                    sb.setSpan(underlineSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                // the card drawn under a link says nothing of itself: what it holds goes at
                // the very end of the message, once everything else has been said
                if (cell.hasLinkPreview) {
                    final CharSequence preview = cell.linkPreviewAccessibilityText();
                    if (!TextUtils.isEmpty(preview)) {
                        sb.append("\n");
                        sb.append(getString(R.string.LinkPreview));
                        sb.append(", ");
                        sb.append(preview);
                    }
                }
                cell.accessibilityText = sb;
                cell.accessibilityTextUnread = unread;
                cell.accessibilityTextContentUnread = contentUnread;
                cell.accessibilityTextFileSize = fileSize;
                cell.accessibilityTextButtonState = cell.buttonState;
                cell.accessibilityTextMiniButtonState = cell.miniButtonState;
                cell.accessibilityTextMediaDownloaded = mediaDownloaded;
            }

            // the progress moves while the message stays as it is, so it cannot live in the
            // text that is kept: it goes in where it belongs every time the message is read
            CharSequence spokenText = cell.accessibilityText;
            final CharSequence transferProgress = cell.getTransferProgressText();
            if (transferProgress != null && cell.accessibilityTextTransferIndex >= 0 && cell.accessibilityTextTransferIndex <= cell.accessibilityText.length()) {
                final SpannableStringBuilder withProgress = new SpannableStringBuilder(cell.accessibilityText);
                withProgress.insert(cell.accessibilityTextTransferIndex, "\n" + transferProgress);
                spokenText = withProgress;
            }

            final CharSequence timer = cell.getAccessibilityTimerText();
            if (timer != null) {
                final SpannableStringBuilder withTimer = new SpannableStringBuilder(spokenText);
                final int at = cell.accessibilityTextTransferIndex >= 0 && cell.accessibilityTextTransferIndex <= withTimer.length() ? cell.accessibilityTextTransferIndex : withTimer.length();
                withTimer.insert(at, ", " + timer);
                spokenText = withTimer;
            }

            CharSequence playbackPosition = MediaController.getPlaybackPositionDescription(cell.currentMessageObject);
            if (playbackPosition != null) {
                // while the message is being read, keep reporting the position it was reached
                // at: a position that moves on every request looks like the message itself
                // changed and has screen readers read all of it again
                if (cell.isExploredByAccessibility() && !MediaController.getInstance().isMessagePaused()) {
                    if (cell.readPlaybackPosition == null) {
                        cell.readPlaybackPosition = playbackPosition;
                    }
                    playbackPosition = cell.readPlaybackPosition;
                } else {
                    cell.readPlaybackPosition = null;
                }
                spokenText = TextUtils.concat(playbackPosition, ", ", spokenText);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                info.setContentDescription(spokenText.toString());
            } else {
                info.setText(spokenText);
                // links are only exposed to screen readers through the node text, so keep
                // the content description off the node to not shadow it
                info.setContentDescription(null);
            }
            if (!cell.isExploredByAccessibility()) {
                cell.updateAccessibilityTraversalText();
            }
            info.setMovementGranularities(
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_CHARACTER |
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD |
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_PARAGRAPH
            );
            info.addAction(AccessibilityNodeInfo.ACTION_NEXT_AT_MOVEMENT_GRANULARITY);
            info.addAction(AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY);

            info.setEnabled(true);
            AccessibilityNodeInfo.CollectionItemInfo itemInfo = info.getCollectionItemInfo();
            if (itemInfo != null) {
                info.setCollectionItemInfo(AccessibilityNodeInfo.CollectionItemInfo.obtain(itemInfo.getRowIndex(), 1, 0, 1, false));
            }
            info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_msg_options, getString("AccActionMessageOptions", R.string.AccActionMessageOptions)));
            if (cell.hasSenderAvatarMenu()) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_sender_avatar_menu, getString(R.string.AccActionSenderOptions)));
            }
            final CharSequence mediaActionLabel = cell.mediaAccessibilityActionLabel();
            if (cell.checkBoxVisible) {
                // a tick is drawn beside this message, so the chat is choosing messages and
                // this is one it can take. Whether it has been taken was drawn and never said,
                // so going back over them told nothing of which had been chosen
                final boolean selected = cell.delegate != null && cell.delegate.isMessageSelected(cell.currentMessageObject);
                info.setCheckable(true);
                info.setChecked(selected);
                final CharSequence selectLabel = getString(selected ? R.string.Deselect : R.string.Select);
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, selectLabel));
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, selectLabel));
                // what a press would have done to the media is not lost while messages are
                // being chosen: it can be asked for by name, and only where a message carries
                // something to play, open or fetch
                if (mediaActionLabel != null) {
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_media, mediaActionLabel));
                }
            } else {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_CLICK, mediaActionLabel));
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(AccessibilityNodeInfo.ACTION_LONG_CLICK, getString("AccActionEnterSelectionMode", R.string.AccActionEnterSelectionMode)));
            }
            if (cell.hasMediaDownloadAction()) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_download, getString(cell.isMediaDownloading() ? R.string.AccActionCancelDownload : R.string.AccActionDownload)));
            }
            int smallIcon = cell.getMiniIconForCurrentState();
            if (smallIcon == MediaActionDrawable.ICON_DOWNLOAD) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_small_button, getString("AccActionDownload", R.string.AccActionDownload)));
            }
            if (!TextUtils.isEmpty(cell.accessibilityViaBotText)) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_via_bot, cell.accessibilityViaBotText));
            }
            if (cell.drawSummarizeButton || cell.drawSummaryReply) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_summarize, getString("SummaryTitle", R.string.SummaryTitle)));
            }
            if (cell.hasSignedPostAuthorAction()) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_sender_profile, cell.getSignedPostAuthorActionLabel()));
            }

            if ((cell.currentMessageObject.isVoice() || cell.currentMessageObject.isRoundVideo() || cell.currentMessageObject.isMusic()) && MediaController.getInstance().isPlayingMessage(cell.currentMessageObject)) {
                cell.seekBarAccessibilityDelegate.onInitializeAccessibilityNodeInfoInternal(info);
            }

            cell.reportedVirtualViewIds.clear();

            if (cell.useTranscribeButton && cell.transcribeButton != null) {
                info.addChild(cell, TRANSCRIBE);
                cell.reportedVirtualViewIds.add(TRANSCRIBE);
            }

            int i;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (cell.isChat && cell.currentUser != null && !cell.currentMessageObject.isOut()) {
                    info.addChild(cell, PROFILE);
                    cell.reportedVirtualViewIds.add(PROFILE);
                }
                if (cell.currentMessageObject.messageText instanceof Spannable) {
                    Spannable buffer = (Spannable) cell.currentMessageObject.messageText;
                    CharacterStyle[] links = buffer.getSpans(0, buffer.length(), ClickableSpan.class);
                    i = 0;
                    for (CharacterStyle link : links) {
                        info.addChild(cell, LINK_IDS_START + i);
                        cell.reportedVirtualViewIds.add(LINK_IDS_START + i);
                        i++;
                    }
                }
                if (cell.currentMessageObject.caption instanceof Spannable && cell.captionLayout != null) {
                    Spannable buffer = (Spannable) cell.currentMessageObject.caption;
                    CharacterStyle[] links = buffer.getSpans(0, buffer.length(), ClickableSpan.class);
                    i = 0;
                    for (CharacterStyle link : links) {
                        info.addChild(cell, LINK_CAPTION_IDS_START + i);
                        cell.reportedVirtualViewIds.add(LINK_CAPTION_IDS_START + i);
                        i++;
                    }
                }
            }
            i = 0;
            for (BotButton button : cell.botButtons) {
                info.addChild(cell, BOT_BUTTONS_START + i);
                cell.reportedVirtualViewIds.add(BOT_BUTTONS_START + i);
                i++;
            }
            if (cell.hintButtonVisible && cell.pollHintX != -1 && cell.currentMessageObject.isPoll()) {
                info.addChild(cell, POLL_HINT);
                cell.reportedVirtualViewIds.add(POLL_HINT);
            }
            i = 0;
            for (ChatMessageCell.PollButton button : cell.pollButtons) {
                info.addChild(cell, POLL_BUTTONS_START + i);
                cell.reportedVirtualViewIds.add(POLL_BUTTONS_START + i);
                i++;
            }
            // a poll that lets anyone add an answer has a button under the last one for it.
            // It is a control of its own, drawn like the answers above it, and there was
            // nothing of it in the tree at all: it could be neither found nor pressed
            // whether a poll lets answers be added is worked out for a poll alone, so a cell
            // used again for a message that is not one keeps the answer of the poll it held
            // before. The button is drawn for a poll only; ask for one here as well
            if ((cell.poll != null && cell.poll.allowAdding) && cell.poll.addButtonDrawable != null && cell.currentMessageObject.isPoll()) {
                info.addChild(cell, POLL_ADD_OPTION);
                cell.reportedVirtualViewIds.add(POLL_ADD_OPTION);
            }
            final GiveawayAccessibilityCard giveawayCard = cell.giveawayAccessibilityCard();
            if (giveawayCard != null) {
                for (int g = 0; g < giveawayCard.getAccessibilityButtonCount(); g++) {
                    if (!TextUtils.isEmpty(giveawayCard.getAccessibilityButtonTitle(g))) {
                        info.addChild(cell, GIVEAWAY_BUTTONS_START + g);
                        cell.reportedVirtualViewIds.add(GIVEAWAY_BUTTONS_START + g);
                    }
                }
            }
            // the button that opens the rest of a message cut short. It is drawn by hand
            // under the text and is no view of its own, so nothing said the message went on,
            // and there was no way to ask for the rest of it
            if (cell.hasShowMoreButton()) {
                info.addChild(cell, SHOW_MORE);
            }
            if (cell.drawInstantView && !cell.instantButtonAccessibilityRect.isEmpty()) {
                info.addChild(cell, INSTANT_VIEW);
                cell.reportedVirtualViewIds.add(INSTANT_VIEW);
            }
            if (cell.contact != null && cell.contact.visible && cell.contactRect != null && !cell.contactRect.isEmpty()) {
                info.addChild(cell, CONTACT);
                cell.reportedVirtualViewIds.add(CONTACT);
                if (cell.contact.buttons != null && cell.contact.buttons.size() > 1) {
                    for (ChatMessageCell.InstantViewButton instantViewButton : cell.contact.buttons) {
                        if (cell.contact.drawView && instantViewButton.type == ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_VIEW && !instantViewButton.rect.isEmpty()) {
                            info.addChild(cell, CONTACT_VIEW);
                            cell.reportedVirtualViewIds.add(CONTACT_VIEW);
                        }
                        if (cell.contact.drawAdd && instantViewButton.type == ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_ADD && !instantViewButton.rect.isEmpty()) {
                            info.addChild(cell, CONTACT_ADD);
                            cell.reportedVirtualViewIds.add(CONTACT_ADD);
                        }
                        if (cell.contact.drawSendMessage && instantViewButton.type == ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_SEND_MESSAGE && !instantViewButton.rect.isEmpty()) {
                            info.addChild(cell, CONTACT_MESSAGE);
                            cell.reportedVirtualViewIds.add(CONTACT_MESSAGE);
                        }
                    }
                }
            }
            if (cell.commentLayout != null) {
                info.addChild(cell, COMMENT);
                cell.reportedVirtualViewIds.add(COMMENT);
            }
            if (cell.drawSideButton == 1 || cell.drawSideButton == 2) {
                info.addChild(cell, SHARE);
                cell.reportedVirtualViewIds.add(SHARE);
            }
            if (cell.replyNameLayout != null) {
                info.addChild(cell, REPLY);
                cell.reportedVirtualViewIds.add(REPLY);
            }
            if (cell.currentMessageObject != null && cell.currentMessageObject.richLayout != null) {
                final RichMessageLayout richLayout = cell.currentMessageObject.richLayout;
                int acc = 0;
                for (int b = 0; b < richLayout.blocks.size(); b++) {
                    final RichMessageLayout.RichBlock block = richLayout.blocks.get(b);
                    if (!block.isVisible()) {
                        continue;
                    }
                    final int count = block.getAccessibilityElementCount();
                    for (int e = 0; e < count; e++) {
                        info.addChild(cell, RICH_MEDIA_START + acc + e);
                        cell.reportedVirtualViewIds.add(RICH_MEDIA_START + acc + e);
                    }
                    acc += count;
                }
            }
            // the channels like the one just joined, on the card under the notice of having joined it
            if (cell.channelRecommendationsCell != null && cell.currentMessageObject != null && cell.currentMessageObject.type == MessageObject.TYPE_JOINED_CHANNEL) {
                for (int r = 0, n = cell.channelRecommendationsCell.getAccessibilityElementCount(); r < n; r++) {
                    info.addChild(cell, RECOMMENDATIONS_START + r);
                    cell.reportedVirtualViewIds.add(RECOMMENDATIONS_START + r);
                }
            }
            if (cell.forwardedNameLayout[0] != null && cell.forwardedNameLayout[1] != null) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_open_forwarded_origin, getString("AccActionOpenForwardedOrigin", R.string.AccActionOpenForwardedOrigin)));
            }
            if (cell.hasOpenableLinkPreview()) {
                // the action carries the card as well, so what the link leads to can be heard
                // from the list of actions without going back through the message for it
                final CharSequence preview = cell.linkPreviewAccessibilityText();
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.acc_action_open_link_preview,
                    TextUtils.isEmpty(preview) ? getString(R.string.OpenUrlTitle) : TextUtils.concat(getString(R.string.OpenUrlTitle), ", ", preview)
                ));
            }

            // opening what a poll carries is a thing to do with the message, so it is an
            // action rather than another stop to swipe past on every poll
            if (cell.hasPollDescriptionMedia()) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.acc_action_open_poll_media,
                    TextUtils.concat(getString(R.string.Open), ", ", cell.pollMediaDescription(cell.poll.contentDrawable.getMedia()))
                ));
            }
            if (cell.hasPollExplanationMedia()) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    R.id.acc_action_open_poll_explanation_media,
                    TextUtils.concat(getString(R.string.Open), ", ", formatString(R.string.AccDescrPollExplanationMedia, cell.pollMediaDescription(cell.poll.explanationDrawable.getMedia())))
                ));
            }

            // every run of monospace text and every block of code that can be copied gets an
            // action of its own, and the action carries what it would copy, so which of them
            // is which is known without having to try one and look at the clipboard after
            cell.collectCopyableTexts();
            for (int c = 0; c < cell.copyableTexts.size(); c++) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    cell.copyTextActionId(c),
                    formatString(R.string.AccActionCopyText, cell.copyActionLabel(cell.copyableTexts.get(c).text))
                ));
            }

            // the reactions come after everything else a message offers: answering one is the
            // last thing wanted of a message, and the actions before them are the ones reached
            // for first
            if (cell.reactionsLayoutInBubble != null) {
                cell.reactionsLayoutInBubble.addAccessibilityActions(info);
            }
            // every link that has a menu when held gets an action that opens it, after
            // everything else the message offers
            cell.collectLinksWithOptions();
            for (int l = 0; l < cell.linksWithOptions.size(); l++) {
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(cell.linkOptionsActionId(l), cell.linkOptionsLabel(cell.linksWithOptions.get(l))));
            }
            // whether a message has been chosen is said once, by the tick it now carries.
            // It was said twice: this called it chosen as well, going by the background drawn
            // behind it rather than by what has actually been chosen, and a cell with any
            // background at all was called chosen whether it was or not
            return info;
        } else {
            AccessibilityNodeInfo info = AccessibilityNodeInfo.obtain();
            info.setSource(cell, virtualViewId);
            info.setParent(cell);
            info.setPackageName(cell.getContext().getPackageName());
            if (virtualViewId == PROFILE) {
                if (cell.currentUser == null) {
                    return null;
                }
                String content = UserObject.getUserName(cell.currentUser);
                info.setText(content);
                rect.set((int) cell.nameX, (int) cell.nameY, (int) (cell.nameX + cell.nameWidth), (int) (cell.nameY + (cell.nameLayout != null ? cell.nameLayout.getHeight() : 10)));
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClassName("android.widget.TextView");
                info.setEnabled(true);
                info.setClickable(true);
                info.setLongClickable(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            } else if (virtualViewId >= RECOMMENDATIONS_START) {
                final int index = virtualViewId - RECOMMENDATIONS_START;
                if (cell.channelRecommendationsCell == null || index >= cell.channelRecommendationsCell.getAccessibilityElementCount()) {
                    return null;
                }
                info.setText(cell.channelRecommendationsCell.getAccessibilityElementText(index));
                cell.channelRecommendationsCell.getAccessibilityElementBounds(index, rect);
                info.setBoundsInParent(rect);
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClassName("android.widget.Button");
                // a channel scrolled out of the card is still one to come to, and is scrolled in then
                info.setVisibleToUser(true);
                info.setEnabled(true);
                info.setClickable(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (!cell.channelRecommendationsCell.isAccessibilityElementClose(index)) {
                    info.setLongClickable(true);
                    info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                }
            } else if (virtualViewId >= RICH_MEDIA_START) {
                final int[] localElement = {0};
                final RichMessageLayout.RichBlock block = resolveRichElement(virtualViewId, localElement);
                if (block == null) {
                    return null;
                }
                info.setText(block.getAccessibilityElementText(localElement[0]));
                block.getAccessibilityElementBounds(localElement[0], rect);
                rect.offset(cell.textX, cell.textY);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                final boolean checkbox = block.isAccessibilityElementCheckbox(localElement[0]);
                if (checkbox) {
                    info.setClassName("android.widget.CheckBox");
                } else if (block.isAccessibilityElementButton(localElement[0])) {
                    info.setClassName("android.widget.Button");
                } else if (block.isAccessibilityElementText(localElement[0])) {
                    info.setClassName("android.widget.TextView");
                } else {
                    info.setClassName("android.widget.ImageView");
                }
                info.setEnabled(true);
                if (checkbox) {
                    info.setCheckable(true);
                    info.setChecked(block.isAccessibilityElementChecked(localElement[0]));
                }
                final boolean clickable = block.isAccessibilityElementClickable(localElement[0]);
                info.setClickable(clickable);
                if (clickable) {
                    info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                block.onInitializeAccessibilityElementInfo(localElement[0], info);
                // a slideshow is turned by dragging alone, which a screen reader cannot do
                final int slides = block.getAccessibilitySlideCount(localElement[0]);
                if (slides > 1) {
                    final int slide = block.getAccessibilitySlide(localElement[0]);
                    if (slide + 1 < slides) {
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_slide_next, getString(R.string.Next) + ", " + formatString(R.string.Of, slide + 2, slides)));
                    }
                    if (slide > 0) {
                        info.addAction(new AccessibilityNodeInfo.AccessibilityAction(R.id.acc_action_slide_previous, getString(R.string.AccDescrPrevious) + ", " + formatString(R.string.Of, slide, slides)));
                    }
                }
            } else if (virtualViewId >= LINK_CAPTION_IDS_START) {
                if (!(cell.currentMessageObject.caption instanceof Spannable) || cell.captionLayout == null) {
                    return null;
                }
                Spannable buffer = (Spannable) cell.currentMessageObject.caption;
                ClickableSpan link = getLinkById(virtualViewId, false);
                if (link == null) {
                    return null;
                }
                int[] linkPos = cell.getRealSpanStartAndEnd(buffer, link);
                String content = buffer.subSequence(linkPos[0], linkPos[1]).toString();
                info.setText(content);
                for (MessageObject.TextLayoutBlock block : cell.captionLayout.textLayoutBlocks) {
                    int length = block.textLayout.getText().length();
                    if (block.charactersOffset <= linkPos[0] && block.charactersOffset + length >= linkPos[1]) {
                        block.textLayout.getSelectionPath(linkPos[0] - block.charactersOffset, linkPos[1] - block.charactersOffset, linkPath);
                        linkPath.computeBounds(rectF, true);
                        rect.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                        rect.offset(0, (int) block.textYOffset(cell.captionLayout.textLayoutBlocks, cell.transitionParams));
                        rect.offset(cell.textX, cell.textY);
                        info.setBoundsInParent(rect);
                        if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null) {
                            cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                        }
                        rect.offset(pos[0], pos[1]);
                        info.setBoundsInScreen(rect);
                        break;
                    }
                }

                info.setClassName("android.widget.TextView");
                info.setEnabled(true);
                info.setClickable(true);
                info.setLongClickable(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            } else if (virtualViewId >= LINK_IDS_START) {
                if (!(cell.currentMessageObject.messageText instanceof Spannable)) {
                    return null;
                }
                Spannable buffer = (Spannable) cell.currentMessageObject.messageText;
                ClickableSpan link = getLinkById(virtualViewId, false);
                if (link == null) {
                    return null;
                }
                int[] linkPos = cell.getRealSpanStartAndEnd(buffer, link);
                String content = buffer.subSequence(linkPos[0], linkPos[1]).toString();
                info.setText(content);
                for (MessageObject.TextLayoutBlock block : cell.currentMessageObject.textLayoutBlocks) {
                    int length = block.textLayout.getText().length();
                    if (block.charactersOffset <= linkPos[0] && block.charactersOffset + length >= linkPos[1]) {
                        block.textLayout.getSelectionPath(linkPos[0] - block.charactersOffset, linkPos[1] - block.charactersOffset, linkPath);
                        linkPath.computeBounds(rectF, true);
                        rect.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
                        rect.offset(0, (int) block.textYOffset(cell.currentMessageObject.textLayoutBlocks, cell.transitionParams));
                        rect.offset(cell.textX, cell.textY);
                        info.setBoundsInParent(rect);
                        if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null) {
                            cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                        }
                        rect.offset(pos[0], pos[1]);
                        info.setBoundsInScreen(rect);
                        break;
                    }
                }

                info.setClassName("android.widget.TextView");
                info.setEnabled(true);
                info.setClickable(true);
                info.setLongClickable(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
            } else if (virtualViewId >= BOT_BUTTONS_START) {
                int buttonIndex = virtualViewId - BOT_BUTTONS_START;
                if (buttonIndex >= cell.botButtons.size()) {
                    return null;
                }
                BotButton button = cell.botButtons.get(buttonIndex);
                if (button.isSeparator) {
                    return null;
                }

                info.setText(button.title.getText());
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                info.setClickable(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);

                rect.set((int) (button.x * cell.widthForButtons), button.y, (int) ((button.x + button.width) * cell.widthForButtons), button.y + button.height);
                int addX;
                if (cell.currentMessageObject.isOutOwner()) {
                    addX = cell.getMeasuredWidth() - cell.getWidthForButtons() - dp(10);
                } else {
                    addX = cell.backgroundDrawableLeft + dp(cell.mediaBackground ? 1 : 7);
                }
                rect.offset(addX, cell.layoutHeight);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
            } else if (virtualViewId >= POLL_BUTTONS_START) {
                int buttonIndex = virtualViewId - POLL_BUTTONS_START;
                if (buttonIndex >= cell.pollButtons.size()) {
                    return null;
                }
                ChatMessageCell.PollButton button = cell.pollButtons.get(buttonIndex);
                StringBuilder sb = new StringBuilder(button.title.getText());
                final boolean answerHasMedia = button.answer != null && button.answer.media != null
                    && button.pollButtonDrawable != null && button.pollButtonDrawable.isHasMedia();
                if (answerHasMedia) {
                    sb.append(", ").append(cell.pollMediaDescription(button.answer.media));
                }
                if (button.task != null) {
                    // an item of a checklist is ticked or it is not, and that is the whole of
                    // what it says. Nothing of it reached a screen reader: a list with half of
                    // it done read exactly like an untouched one. Where the list cannot be
                    // ticked at all a bullet is drawn instead of a box, so it is not offered
                    // as one either, and what is done is said in words, as the line through
                    // the title says it on the screen
                    if (cell.currentMessageObject.canCompleteTodo()) {
                        info.setClassName("android.widget.CheckBox");
                        info.setCheckable(true);
                        info.setChecked(cell.isTodoItemDone(buttonIndex, button));
                        if (button.author != null && cell.isTodoItemDone(buttonIndex, button)) {
                            sb.append(", ").append(formatString(R.string.AccDescrTodoDoneBy, button.author.getText()));
                        }
                    } else {
                        info.setClassName("android.widget.TextView");
                        if (cell.isTodoItemDone(buttonIndex, button)) {
                            sb.append(", ").append(getString(R.string.AccDescrTodoDone));
                        }
                    }
                } else if (cell.isPollOptionTickable(button)) {
                    // the tick beside an option is state, so it is left to the screen reader
                    // to say in its own words rather than written into the text
                    info.setClassName("android.widget.CheckBox");
                    info.setCheckable(true);
                    info.setChecked(cell.isPollOptionTicked(buttonIndex, button));
                } else if (!cell.pollVoted) {
                    info.setClassName("android.widget.Button");
                } else {
                    info.setSelected(button.chosen);
                    sb.append(", ").append(button.percent).append("%");
                    if (button.answer != null) {
                        // the number beside the bar, which only the bar carried until now
                        sb.append(", ").append(formatPluralString(cell.lastPoll != null && cell.lastPoll.quiz ? "Answer" : "Vote", button.count));
                    }
                    if (cell.lastPoll != null && cell.lastPoll.quiz && (button.chosen || button.correct)) {
                        sb.append(", ").append(button.correct ? getString("AccDescrQuizCorrectAnswer", R.string.AccDescrQuizCorrectAnswer) : getString("AccDescrQuizIncorrectAnswer", R.string.AccDescrQuizIncorrectAnswer));
                    }
                }
                final CharSequence answerVoters = cell.pollRecentVoterNames(button.recentVoters);
                if (answerVoters != null) {
                    sb.append(", ").append(answerVoters);
                }
                info.setText(sb);
                info.setEnabled(true);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                // a long press on an option opens the menu that belongs to it, where who
                // added it, quoting it and removing it are. A press held on the screen got
                // there; nothing offered it to a screen reader
                if (cell.delegate != null && (button.answer != null || button.task != null)) {
                    info.setLongClickable(true);
                    info.addAction(AccessibilityNodeInfo.ACTION_LONG_CLICK);
                }
                if (answerHasMedia) {
                    // the picture of an answer opens on its own; a press on the answer votes.
                    // The two are kept apart here as they are on the screen
                    info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                        R.id.acc_action_open_poll_media,
                        TextUtils.concat(getString(R.string.Open), ", ", cell.pollMediaDescription(button.answer.media))
                    ));
                }

                final int y = button.y + cell.namesOffset;
                final int w = cell.backgroundWidth - dp(76);
                rect.set(button.x, y, button.x + w, y + button.height);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);

                info.setClickable(true);
            } else if (virtualViewId == POLL_HINT) {
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                // the same button opens the explanation and closes it again, so which of the
                // two it is now has to be said on the button itself: a name alone leaves no
                // way of knowing whether pressing it will open or close
                final boolean explanationOpen = cell.currentMessageObject != null && cell.currentMessageObject.expandedExplanation;
                info.setText(TextUtils.concat(
                    getString(R.string.AccDescrQuizExplanation), ", ",
                    getString(explanationOpen ? R.string.AccDescrExpanded : R.string.AccDescrCollapsed)
                ));
                info.addAction(new AccessibilityNodeInfo.AccessibilityAction(
                    AccessibilityNodeInfo.ACTION_CLICK,
                    getString(explanationOpen ? R.string.PollCollapse : R.string.PollExpand)
                ));
                rect.set(cell.pollHintX - dp(8), cell.pollHintY - dp(8), cell.pollHintX + dp(32), cell.pollHintY + dp(32));
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == POLL_ADD_OPTION) {
                if (!cell.getPollAddButtonBounds(rect)) {
                    return null;
                }
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                info.setText(getString(R.string.PollAddAnOption));
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                info.setBoundsInParent(rect);
                // the bounds go into the map the cell hit tests against, so the button is
                // found by a finger on the screen as well as by swiping to it
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId >= GIVEAWAY_BUTTONS_START && virtualViewId < GIVEAWAY_BUTTONS_END) {
                final int index = virtualViewId - GIVEAWAY_BUTTONS_START;
                final GiveawayAccessibilityCard giveawayCard = cell.giveawayAccessibilityCard();
                final CharSequence title = giveawayCard == null ? null : giveawayCard.getAccessibilityButtonTitle(index);
                final Rect bounds = giveawayCard == null ? null : giveawayCard.getAccessibilityButtonBounds(index);
                if (TextUtils.isEmpty(title) || bounds == null || bounds.isEmpty()) {
                    return null;
                }
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                info.setText(title);
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                rect.set(bounds);
                info.setBoundsInParent(rect);
                // the bounds go into the map the cell hit tests against, so a finger on the
                // screen finds them as well as a swipe
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == SHOW_MORE) {
                if (!cell.hasShowMoreButton()) {
                    return null;
                }
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                info.setText(getString(R.string.ShowMore));
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                cell.currentMessageObject.richLayout.getShowMoreBounds(rect);
                rect.offset(cell.textX, cell.textY);
                info.setBoundsInParent(rect);
                // the bounds go into the map the cell hit tests against, so a finger on the
                // screen finds it as well as a swipe
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == INSTANT_VIEW) {
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                if (cell.instantViewLayout != null) {
                    info.setText(cell.instantViewLayout.getText());
                }
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                cell.instantButtonAccessibilityRect.round(rect);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == CONTACT) {
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                if (cell.titleLayout != null) {
                    info.setText(cell.titleLayout.getText());
                }
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                cell.contactRect.round(rect);
                if (cell.contact != null && cell.contact.buttons != null && cell.contact.buttons.size() > 1) {
                    ChatMessageCell.InstantViewButton instantViewButton = cell.contact.buttons.get(0);
                    if (!instantViewButton.rect.isEmpty()) {
                        rect.set(rect.left, rect.top, rect.right, (int) (rect.bottom - instantViewButton.rect.height()));
                    }
                }
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == CONTACT_VIEW || virtualViewId == CONTACT_ADD || virtualViewId == CONTACT_MESSAGE) {
                int requiredType;
                if (virtualViewId == CONTACT_VIEW) {
                    requiredType = ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_VIEW;
                } else if (virtualViewId == CONTACT_ADD) {
                    requiredType = ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_ADD;
                } else {
                    requiredType = ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_SEND_MESSAGE;
                }
                for (int i = 0; cell.contact != null && i < cell.contact.buttons.size(); i++) {
                    ChatMessageCell.InstantViewButton instantViewButton = cell.contact.buttons.get(i);
                    if (instantViewButton.type == requiredType) {
                        info.setClassName("android.widget.Button");
                        info.setEnabled(true);
                        if (instantViewButton.layout != null) {
                            info.setText(instantViewButton.layout.getText());
                        }
                        info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                        instantViewButton.rect.round(rect);
                        info.setBoundsInParent(rect);
                        if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                            cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                        }
                        rect.offset(pos[0], pos[1]);
                        info.setBoundsInScreen(rect);
                        info.setClickable(true);
                        break;
                    }
                }
            } else if (virtualViewId == SHARE) {
                info.setClassName("android.widget.ImageButton");
                info.setEnabled(true);
                if (cell.isOpenChatByShare(cell.currentMessageObject)) {
                    info.setContentDescription(getString("AccDescrOpenChat", R.string.AccDescrOpenChat));
                } else {
                    info.setContentDescription(getString("ShareFile", R.string.ShareFile));
                }
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                rect.set((int) cell.sideStartX, (int) cell.sideStartY, (int) cell.sideStartX + dp(40), (int) cell.sideStartY + dp(32));
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == REPLY) {
                info.setEnabled(true);
                StringBuilder sb = new StringBuilder();
                // the same place over a message holds a reply or, on a forwarded message, where
                // it was forwarded from: it was called a reply either way
                sb.append(getString(cell.replyPanelIsForward ? R.string.AccDescrForwarding : R.string.Reply));
                sb.append(", ");
                if (cell.replyNameLayout != null) {
                    sb.append(cell.replyNameLayout.getText());
                    sb.append(", ");
                }
                if (cell.replyTextLayout != null) {
                    sb.append(cell.replyTextLayout.getText());
                }
                info.setContentDescription(sb.toString());
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);

                rect.set(cell.replyStartX, cell.replyStartY, cell.replyStartX + Math.max(cell.replyNameWidth, cell.replyTextWidth), cell.replyStartY + (int) cell.replyHeight);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == FORWARD) {
                info.setEnabled(true);
                StringBuilder sb = new StringBuilder();
                if (cell.forwardedNameLayout[0] != null && cell.forwardedNameLayout[1] != null) {
                    for (int a = 0; a < 2; a++) {
                        sb.append(cell.forwardedNameLayout[a].getText());
                        sb.append(a == 0 ? " " : "\n");
                    }
                }
                info.setContentDescription(sb.toString());
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);

                int x = (int) Math.min(cell.forwardNameX - cell.forwardNameOffsetX[0], cell.forwardNameX - cell.forwardNameOffsetX[1]);
                rect.set(x, cell.forwardNameY, x + cell.forwardedNameWidth, cell.forwardNameY + cell.forwardHeight);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == COMMENT) {
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                int commentCount = cell.getRepliesCount();
                String comment = null;
                if (cell.currentMessageObject != null && !cell.currentMessageObject.shouldDrawWithoutBackground() && !cell.currentMessageObject.isAnimatedEmoji()) {
                    if (cell.isRepliesChat) {
                        comment = getString("ViewInChat", R.string.ViewInChat);
                    } else {
                        comment = commentCount == 0 ? getString("LeaveAComment", R.string.LeaveAComment) : formatPluralString("CommentsCount", commentCount);
                    }
                } else if (!cell.isRepliesChat && commentCount > 0) {
                    comment = LocaleController.formatShortNumber(commentCount, null);
                }
                if (comment != null) {
                    info.setText(comment);
                }
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                rect.set(cell.commentButtonRect);
                info.setBoundsInParent(rect);
                if (cell.accessibilityVirtualViewBounds.get(virtualViewId) == null || !cell.accessibilityVirtualViewBounds.get(virtualViewId).equals(rect)) {
                    cell.accessibilityVirtualViewBounds.put(virtualViewId, new Rect(rect));
                }
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            } else if (virtualViewId == TRANSCRIBE) {
                info.setClassName("android.widget.Button");
                info.setEnabled(true);
                info.setText(cell.currentMessageObject.isVoiceTranscriptionOpen() ? getString("AccActionCloseTranscription", R.string.AccActionCloseTranscription) : getString("AccActionOpenTranscription", R.string.AccActionOpenTranscription));
                info.addAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (cell.transcribeButton != null) {
                    rect.set((int) cell.ensureAudio().transcribeX, (int) cell.ensureAudio().transcribeY, (int) (cell.ensureAudio().transcribeX + cell.transcribeButton.width()), (int) (cell.ensureAudio().transcribeY + cell.transcribeButton.height()));
                }
                info.setBoundsInParent(rect);
                rect.offset(pos[0], pos[1]);
                info.setBoundsInScreen(rect);
                info.setClickable(true);
            }
            // a message that is not laid out yet, or is being held for reuse, has buttons with
            // no place on the screen: reporting them leaves empty stops that a screen reader
            // lands on and that get in the way of going back through the messages
            info.getBoundsInScreen(rect);
            if (rect.isEmpty()) {
                return null;
            }
            // and the ones that do have a place are tied to the one before them, so going
            // back through them follows the same path as going forward, whichever screen
            // reader is doing the walking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                final int previous = cell.previousVirtualViewId(virtualViewId);
                if (previous != ChatMessageCell.NO_PREVIOUS_VIRTUAL_VIEW) {
                    info.setTraversalAfter(cell, previous);
                }
            }
            info.setFocusable(true);
            info.setVisibleToUser(cell.isAccessibilityStopOnScreen(rect, pos));
            return info;
        }
    }

    @Override
    public boolean performAction(int virtualViewId, int action, Bundle arguments) {
        // a block that is web content (a widget) is gone through as a screen reader goes through a web page:
        // to the next or previous heading, link, control and the like, among the block's own elements
        if (virtualViewId >= RICH_MEDIA_START && (action == AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT || action == AccessibilityNodeInfo.ACTION_PREVIOUS_HTML_ELEMENT)) {
            final int[] localElement = {0};
            final RichMessageLayout.RichBlock block = resolveRichElement(virtualViewId, localElement);
            if (block != null) {
                final String type = arguments == null ? null : arguments.getString(AccessibilityNodeInfo.ACTION_ARGUMENT_HTML_ELEMENT_STRING);
                final int target = block.findAccessibilityHtmlElement(localElement[0], action == AccessibilityNodeInfo.ACTION_NEXT_HTML_ELEMENT, type);
                if (target >= 0) {
                    return performAction(virtualViewId - localElement[0] + target, AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                }
            }
            return false;
        }
        if (virtualViewId == HOST_VIEW_ID) {
            cell.performAccessibilityAction(action, arguments);
        } else {
            if (action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS) {
                cell.accessibilityFocusedStop = virtualViewId;
                if (virtualViewId >= RECOMMENDATIONS_START && cell.channelRecommendationsCell != null) {
                    cell.channelRecommendationsCell.scrollToAccessibilityElement(virtualViewId - RECOMMENDATIONS_START);
                }
                cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
            } else if (action == AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
                if (cell.accessibilityFocusedStop == virtualViewId) {
                    cell.accessibilityFocusedStop = ChatMessageCell.NO_ACCESSIBILITY_STOP;
                }
            } else if (action == AccessibilityNodeInfo.ACTION_CLICK) {
                if (virtualViewId == PROFILE) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressUserAvatar(cell, cell.currentUser, 0, 0, false);
                    }
                } else if (virtualViewId >= RECOMMENDATIONS_START) {
                    if (cell.channelRecommendationsCell != null && cell.channelRecommendationsCell.onAccessibilityElementClick(virtualViewId - RECOMMENDATIONS_START, false)) {
                        cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                    }
                } else if (virtualViewId >= RICH_MEDIA_START) {
                    final int[] localElement = {0};
                    final RichMessageLayout.RichBlock block = resolveRichElement(virtualViewId, localElement);
                    if (block != null && block.onAccessibilityElementClick(localElement[0], cell)) {
                        cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                        AndroidUtilities.makeAccessibilityAnnouncement(block.getAccessibilityElementStateDescription(localElement[0]));
                    }
                } else if (virtualViewId >= LINK_CAPTION_IDS_START) {
                    ClickableSpan link = getLinkById(virtualViewId, true);
                    if (link != null) {
                        cell.delegate.didPressUrl(cell, link, false);
                        cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                    }
                } else if (virtualViewId >= LINK_IDS_START) {
                    ClickableSpan link = getLinkById(virtualViewId, false);
                    if (link != null) {
                        cell.delegate.didPressUrl(cell, link, false);
                        cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                    }
                } else if (virtualViewId >= BOT_BUTTONS_START) {
                    int buttonIndex = virtualViewId - BOT_BUTTONS_START;
                    if (buttonIndex >= cell.botButtons.size()) {
                        return false;
                    }
                    BotButton button = cell.botButtons.get(buttonIndex);
                    if (cell.delegate != null && !button.isLocked) {
                        if (button.buttonCustom != null) {
                            cell.delegate.didPressCustomBotButton(cell, button.buttonCustom);
                        } else if (button.button != null) {
                            cell.delegate.didPressBotButton(cell, button.button);
                        }
                    }
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                } else if (virtualViewId >= POLL_BUTTONS_START) {
                    int buttonIndex = virtualViewId - POLL_BUTTONS_START;
                    if (buttonIndex >= cell.pollButtons.size()) {
                        return false;
                    }
                    ChatMessageCell.PollButton button = cell.pollButtons.get(buttonIndex);
                    // a press went straight to sending a vote, whatever the option was: an
                    // option of a poll that takes more than one answer was sent on its own
                    // instead of being ticked, and one of a poll already voted in or closed
                    // sent a vote again instead of opening the menu it has. Do what a press
                    // on the option itself does
                    if (cell.delegate != null) {
                        cell.performPollButtonPress(buttonIndex);
                    }
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                } else if (virtualViewId == POLL_ADD_OPTION) {
                    if (cell.delegate != null && cell.currentMessageObject.isPoll()) {
                        cell.delegate.didPressAddPollOptionButton(cell);
                    }
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                } else if (virtualViewId == POLL_HINT) {
                    cell.didPressVoteHint();
                } else if (virtualViewId == SHOW_MORE) {
                    if (cell.delegate != null && cell.hasShowMoreButton()) {
                        cell.delegate.didPressShowMore(cell);
                    }
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                } else if (virtualViewId >= GIVEAWAY_BUTTONS_START && virtualViewId < GIVEAWAY_BUTTONS_END) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressGiveawayChatButton(cell, virtualViewId - GIVEAWAY_BUTTONS_START);
                    }
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_CLICKED);
                } else if (virtualViewId == INSTANT_VIEW) {
                    if (cell.lastPoll != null) {
                        cell.performPollInstantButton();
                    } else if (cell.delegate != null) {
                        cell.delegate.didPressInstantButton(cell, cell.drawInstantViewType);
                    }
                } else if (virtualViewId == CONTACT) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressInstantButton(cell, ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_VIEW);
                    }
                } else if (virtualViewId == CONTACT_VIEW) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressInstantButton(cell, ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_VIEW);
                    }
                } else if (virtualViewId == CONTACT_ADD) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressInstantButton(cell, ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_ADD);
                    }
                } else if (virtualViewId == CONTACT_MESSAGE) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressInstantButton(cell, ChatMessageCell.INSTANT_BUTTON_TYPE_CONTACT_SEND_MESSAGE);
                    }
                } else if (virtualViewId == SHARE) {
                    if (cell.delegate != null) {
                        cell.delegate.didPressSideButton(cell);
                    }
                } else if (virtualViewId == REPLY) {
                    // what is drawn over a message is not always a reply: where it is a
                    // forward, touching it goes to where the message came from, and that is
                    // what is done here as well
                    if (cell.replyPanelIsForward) {
                        if (cell.delegate != null) {
                            if (cell.currentForwardChannel != null) {
                                cell.delegate.didPressChannelAvatar(cell, cell.currentForwardChannel, cell.currentMessageObject.messageOwner.fwd_from.channel_post, cell.lastTouchX, cell.lastTouchY, false);
                            } else if (cell.currentForwardUser != null) {
                                cell.delegate.didPressUserAvatar(cell, cell.currentForwardUser, cell.lastTouchX, cell.lastTouchY, false);
                            } else if (cell.currentForwardName != null) {
                                cell.delegate.didPressHiddenForward(cell);
                            }
                        }
                    } else if (cell.delegate != null && (cell.currentMessageObject.hasValidReplyMessageObject() || cell.currentMessageObject.isReplyToStory() || cell.hasReplyQuote || cell.currentMessageObject.messageOwner != null && cell.currentMessageObject.messageOwner.reply_to != null && cell.currentMessageObject.messageOwner.reply_to.reply_from != null)) {
                        cell.delegate.didPressReplyMessage(cell, cell.currentMessageObject.getReplyMsgId(), 0, 0, false);
                    }
                } else if (virtualViewId == FORWARD) {
                    if (cell.delegate != null) {
                        if (cell.currentForwardChannel != null) {
                            cell.delegate.didPressChannelAvatar(cell, cell.currentForwardChannel, cell.currentMessageObject.messageOwner.fwd_from.channel_post, cell.lastTouchX, cell.lastTouchY, false);
                        } else if (cell.currentForwardUser != null) {
                            cell.delegate.didPressUserAvatar(cell, cell.currentForwardUser, cell.lastTouchX, cell.lastTouchY, false);
                        } else if (cell.currentForwardName != null) {
                            cell.delegate.didPressHiddenForward(cell);
                        }
                    }
                } else if (virtualViewId == COMMENT) {
                    if (cell.delegate != null) {
                        if (cell.isRepliesChat) {
                            cell.delegate.didPressSideButton(cell);
                        } else {
                            cell.delegate.didPressCommentButton(cell);
                        }
                    }
                } else if (virtualViewId == TRANSCRIBE && cell.transcribeButton != null) {
                    cell.transcribeButton.onTap();
                }
            } else if (action == R.id.acc_action_open_poll_media) {
                if (virtualViewId >= POLL_BUTTONS_START && virtualViewId < BOT_BUTTONS_START) {
                    int buttonIndex = virtualViewId - POLL_BUTTONS_START;
                    if (buttonIndex >= cell.pollButtons.size()) {
                        return false;
                    }
                    ChatMessageCell.PollButton button = cell.pollButtons.get(buttonIndex);
                    if (button.answer != null && button.answer.media != null && button.pollButtonDrawable != null) {
                        final Rect bounds = button.pollButtonDrawable.getBounds();
                        cell.didClickedPollImage(cell, button.pollButtonDrawable.getImageReceiver(), button.answer, button.answer.media, bounds.centerX(), bounds.centerY(), button.answer.unshuffled_index);
                    }
                }
            } else if (action == AccessibilityNodeInfo.ACTION_LONG_CLICK && virtualViewId >= RECOMMENDATIONS_START) {
                if (cell.channelRecommendationsCell != null && cell.channelRecommendationsCell.onAccessibilityElementClick(virtualViewId - RECOMMENDATIONS_START, true)) {
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
                }
            } else if ((action == R.id.acc_action_slide_next || action == R.id.acc_action_slide_previous) && virtualViewId >= RICH_MEDIA_START) {
                final int[] localElement = {0};
                final RichMessageLayout.RichBlock block = resolveRichElement(virtualViewId, localElement);
                if (block != null) {
                    final int slide = block.getAccessibilitySlide(localElement[0]) + (action == R.id.acc_action_slide_next ? 1 : -1);
                    if (block.showAccessibilitySlide(localElement[0], slide)) {
                        cell.invalidate();
                        cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_SCROLLED);
                        AndroidUtilities.makeAccessibilityAnnouncement(block.getAccessibilityElementText(localElement[0]));
                    }
                }
            } else if (action == AccessibilityNodeInfo.ACTION_LONG_CLICK) {
                if (virtualViewId >= POLL_BUTTONS_START && virtualViewId < BOT_BUTTONS_START) {
                    int buttonIndex = virtualViewId - POLL_BUTTONS_START;
                    if (buttonIndex >= cell.pollButtons.size()) {
                        return false;
                    }
                    ChatMessageCell.PollButton button = cell.pollButtons.get(buttonIndex);
                    if (cell.delegate != null) {
                        if (button.task != null) {
                            cell.delegate.didLongPressToDoButton(cell, button.task);
                        } else if (button.answer != null) {
                            cell.delegate.didLongPressPollOption(cell, button.answer);
                        }
                        cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
                    }
                    return true;
                }
                ClickableSpan link = getLinkById(virtualViewId, virtualViewId >= LINK_CAPTION_IDS_START);
                if (link != null && cell.delegate != null) {
                    cell.delegate.didPressUrl(cell, link, true);
                    cell.sendAccessibilityEventForVirtualView(virtualViewId, AccessibilityEvent.TYPE_VIEW_LONG_CLICKED);
                }
            }
        }
        return true;
    }

    private ClickableSpan getLinkById(int id, boolean caption) {
        if (id == PROFILE) {
            return null;
        }
        if (caption) {
            id -= LINK_CAPTION_IDS_START;
            if (!(cell.currentMessageObject.caption instanceof Spannable) || id < 0) {
                return null;
            }
            Spannable buffer = (Spannable) cell.currentMessageObject.caption;
            ClickableSpan[] links = buffer.getSpans(0, buffer.length(), ClickableSpan.class);
            if (links.length <= id) {
                return null;
            }
            return links[id];
        } else {
            id -= LINK_IDS_START;
            if (!(cell.currentMessageObject.messageText instanceof Spannable) || id < 0) {
                return null;
            }
            Spannable buffer = (Spannable) cell.currentMessageObject.messageText;
            ClickableSpan[] links = buffer.getSpans(0, buffer.length(), ClickableSpan.class);
            if (links.length <= id) {
                return null;
            }
            return links[id];
        }
    }
}
