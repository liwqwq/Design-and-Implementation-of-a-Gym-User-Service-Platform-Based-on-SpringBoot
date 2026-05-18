# v58 Community Feed Fix

Fixed the issue where a member could publish a post and the admin side could see it, but the user Activity Feed did not obviously show it.

Changes:
- After publishing a post, the user community page switches to the Latest sort and All category so the new post appears immediately.
- The frontend also adds the returned post to the local feed optimistically before reloading from the backend.
- New posts are created with `visible=true` and saved to MySQL immediately.
- Member `/api/posts` treats old posts with no `visible` field as visible, and hides only posts explicitly marked `visible=false` by admin.
