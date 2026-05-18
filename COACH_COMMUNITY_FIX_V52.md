# V52 Fix Notes - Community Posting and Coach Translation

This version fixes two issues found during testing:

1. The user community "Post Update / 发布动态" button did not open the modal because the admin post editor method used the same method name as the user posting dialog. The admin method has been renamed to avoid the conflict.
2. The coach portal still contained many English-only labels when the Chinese language mode was selected. Coach dashboard, profile, class management, facility booking, student list, mailbox actions, edit dialog, notification settings, and status labels have been updated with Chinese/English conditional text.

The frontend has been rebuilt and the latest static files have been copied into `src/main/resources/static`.
