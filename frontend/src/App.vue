<template>
  <div class="ft-root">
    <div v-if="message && !authPage" class="ft-toast-success">{{ message }}</div>
    <section v-if="authPage" :class="['ft-auth-page', authThemeClass]">
      <div class="ft-login-locale">
        <div class="ft-lang clean-lang"><button @click="setLang('zh')" :class="{on:lang==='zh'}">中文</button><button @click="setLang('en')" :class="{on:lang==='en'}">English</button></div>
      </div>
      <div class="clean-auth-box">
        <aside class="clean-auth-showcase">
          <div class="clean-auth-brand"><span>💪</span><strong>FitLife</strong></div>
          <div class="clean-auth-copy">
            <span class="clean-kicker">{{ t('classes') }}</span>
            <h1>{{ authHeroTitle }}</h1>
            <p>{{ authSubtitle }}</p>
            <div class="clean-feature-grid">
              <div><b>📅 {{ t('classes') }}</b><span>{{ lang==='zh'?'课程预约 · 日程管理':'Class Booking · Schedule' }}</span></div>
              <div><b>🏆 {{ t('points') }}</b><span>{{ t('mall') }} · {{ t('ranking') }}</span></div>
              <div><b>✅ {{ t('checkin') }}</b><span>{{ t('checkin') }} · {{ t('community') }}</span></div>
              <div><b>👨‍🏫 {{ lang==='zh'?'教练':'Coach' }}</b><span>{{ lang==='zh'?'私教课程 · FitLife':'Trainer · FitLife' }}</span></div>
            </div>
          </div>
          <div class="clean-test-panel">
            <h3>🧪 {{ lang==='zh'?'测试账号':'Test Accounts' }}</h3>
            <div v-for="demo in demoUserAccounts" :key="demo.username" :class="['clean-test-card',{active:!route.includes('admin') && !route.includes('coach') && loginForm.username===demo.username}]" @click="fillDemo(demo.key)"><i>{{ demo.icon }}</i><p><b>{{ lang==='zh'?demo.zh:demo.en }}</b><span>{{ demo.username }} / 123456</span></p></div>
            <div :class="['clean-test-card',{active:route.includes('admin')}]" @click="fillDemo('admin')"><i>👔</i><p><b>{{ lang==='zh'?'管理端':'Admin Portal' }}</b><span>admin / admin123</span></p></div>
            <div :class="['clean-test-card',{active:route.includes('coach')}]" @click="fillDemo('coach')"><i>👨‍🏫</i><p><b>{{ lang==='zh'?'教练端':'Coach Portal' }}</b><span>coach_1 / 123456</span></p></div>
          </div>
        </aside>
        <section class="clean-auth-panel">
          <div class="clean-auth-card">
            <div class="clean-card-head">
              <div class="clean-card-badge">{{ route.includes('admin') ? '👔' : route.includes('coach') ? '👨‍🏫' : '💪' }}</div>
              <h2>{{ authTitle }}</h2>
              <p>{{ lang==='zh'?'欢迎来到':'Welcome to' }} FitLife</p>
            </div>
            <div class="ft-alert" v-if="message">❌ {{ message }}</div>
            <form class="clean-login-form" @submit.prevent="submitAuth">
              <label><span>👤 {{ t('username') }}</span><div><i>👤</i><input v-model="loginForm.username" :placeholder="lang==='zh'?'请输入用户名':'Enter username'" autocomplete="username" /></div></label>
              <label><span>🔒 {{ t('password') }}</span><div><i>🔒</i><input v-model="loginForm.password" type="password" :placeholder="lang==='zh'?'请输入密码':'Enter password'" autocomplete="current-password" /></div></label>
              <template v-if="route === '/user/register'">
                <label><span>📝 {{ lang==='zh'?'姓名':'Name' }}</span><div><i>📝</i><input v-model="loginForm.name" :placeholder="lang==='zh'?'姓名':'Name'" /></div></label>
                <label><span>✉️ Email</span><div><i>✉️</i><input v-model="loginForm.email" placeholder="Email" /></div></label>
                <label><span>📱 {{ lang==='zh'?'手机号':'Phone' }}</span><div><i>📱</i><input v-model="loginForm.phone" :placeholder="lang==='zh'?'手机号':'Phone'" /></div></label>
              </template>
              <button class="clean-submit" type="submit" :disabled="authLoading">{{ authLoading ? (lang==='zh'?'登录中...':'Signing in...') : authButton }}</button>
            </form>
            <div class="clean-divider"><span>{{ lang==='zh'?'或者':'or' }}</span></div>
            <div class="clean-role-grid">
              <button v-for="entry in alternateAuthEntries" :key="entry.path" type="button" @click="go(entry.path)"><b>{{ entry.icon }} {{ entry.title }}</b><span>{{ entry.sub }}</span></button>
            </div>
            <div class="clean-register-line">
              <a v-if="route !== '/user/register'" @click="go('/user/register')">{{ lang==='zh'?'没有账号？立即注册':'Create an account' }}</a>
              <a v-else @click="go('/user/login')">{{ lang==='zh'?'已有账号？去登录':'Already have an account? Sign in' }}</a>
            </div>
          </div>
        </section>
      </div>
    </section>

    <template v-else-if="area === 'user'">
      <header class="ft-user-head">
        <div class="ft-head-inner">
          <div class="ft-logo" @click="go('/user/dashboard')"><span>💪</span><strong>FitLife</strong></div>
          <nav class="ft-top-links">
            <a v-for="item in userMenu" :key="item.path" :class="{current: route===item.path}" @click="go(item.path)">{{ t(item.key) }}</a>
          </nav>
          <div class="ft-head-tools">
            <div class="ft-lang"><button @click="setLang('zh')" :class="{on:lang==='zh'}">中文</button><button @click="setLang('en')" :class="{on:lang==='en'}">English</button></div>
            <span class="ft-points">🏆 {{ points }} {{ t('points') }}</span>
            <span class="ft-user-name">{{ userHandle }}</span>
            <button class="ft-exit" @click="logout">{{ t('logout') }}</button>
          </div>
        </div>
      </header>
      <main class="ft-user-main">
        <section v-if="route==='/user/dashboard'" class="ft-page v10-home">
          <div class="v10-hero">
            <div class="v10-blob one"></div><div class="v10-blob two"></div>
            <div class="v10-hero-copy">
              <span class="v10-chip">{{ lang==='zh'?'你的今日健身面板':'Your fitness control center' }}</span>
              <h1>{{ lang==='zh'?'欢迎回来，'+userHandle:'Welcome back, '+userHandle }}</h1>
              <p>{{ lang==='zh'?'查看课程、打卡进度和社区动态，把今天的训练安排得更高效。':'Track classes, check-ins and community updates in one clean dashboard.' }}</p>
              <div class="v10-actions"><button class="v10-light" @click="go('/user/checkin')">🔥 {{ lang==='zh'?'打卡签到':'Check-in Now' }}</button><button class="v10-glass" @click="go('/user/classes')">📅 {{ lang==='zh'?'预约课程':'Book Class' }}</button></div>
            </div>
            <aside class="v10-planbox">
              <div class="v10-planline"><span>{{ lang==='zh'?'今日计划':"Today's Plan" }}</span><b>{{ todayShort }}</b></div>
              <div class="v10-next"><div class="v10-next-icon">{{ iconFor(firstClass.name) }}</div><div><small>{{ lang==='zh'?'推荐课程':'Recommended class' }}</small><h3>{{ pick(firstClass,'name') || (lang==='zh'?'力量训练':'Strength Training') }}</h3><p>{{ timeRange(firstClass) || '09:00' }} · {{ pick(firstClass,'coachName') || '张教练' }}</p></div></div>
              <div class="v10-goal"><div><span>{{ lang==='zh'?'7天打卡目标':'7-day check-in goal' }}</span><b>{{ goalProgress }}%</b></div><i><em :style="{width:goalProgress+'%'}"></em></i><p>{{ lang==='zh'?'持续打卡解锁更多积分和徽章':'Keep checking in to unlock more points and badges' }}</p></div>
            </aside>
          </div>
          <section class="v10-vip" :class="{active:isVip}"><div><span>👑 {{ isVip ? (lang==='zh'?'VIP 已激活':'VIP Active') : (lang==='zh'?'VIP 会员':'VIP Membership') }}</span><h2>{{ isVip ? (lang==='zh'?'你的 VIP 会员正在生效':'Your VIP membership is active') : (lang==='zh'?'升级 VIP，解锁更多健身权益':'Upgrade to VIP for more fitness benefits') }}</h2><p>{{ isVip ? (lang==='zh'?'VIP 到期日：'+(membershipEndDate||'-')+'，可继续续费延长会员有效期。':'VIP expiry date: '+(membershipEndDate||'-')+'. You can renew to extend your membership.') : (lang==='zh'?'购买 VIP 后可获得会员身份标识、VIP 专属商品权限，并提升平台内的会员体验。':'Become a VIP member to unlock VIP identity, VIP mall access, and a better member experience across the platform.') }}</p></div><button @click="go('/user/vip-payment')">{{ isVip ? (lang==='zh'?'续费 VIP':'Extend VIP') : (lang==='zh'?'购买 VIP':'Buy VIP') }}</button></section>
          <div class="v10-stats"><article><span>🏆</span><b>{{ points }}</b><p>{{ lang==='zh'?'可用积分':'Available Points' }}</p></article><article><span>🔥</span><b>{{ dash.streak || 30 }}</b><p>{{ lang==='zh'?'连续打卡':'Consecutive Days' }}</p></article><article><span>🎯</span><b>{{ dash.bookings || 3 }}</b><p>{{ lang==='zh'?'本月预约':'Booked This Month' }}</p></article><article><span>🥇</span><b>{{ dash.badges || 1 }}</b><p>{{ lang==='zh'?'获得徽章':'Badges Earned' }}</p></article></div>
          <div class="v10-feature-row"><button @click="go('/user/classes')"><i>📅</i><b>{{ lang==='zh'?'预约课程':'Book Class' }}</b><small>{{ lang==='zh'?'查看可预约课程':'Find available sessions' }}</small></button><button @click="go('/user/checkin')"><i>✅</i><b>{{ lang==='zh'?'打卡签到':'Check-in Now' }}</b><small>{{ lang==='zh'?'记录今日训练':'Record today\'s visit' }}</small></button><button @click="go('/user/points-mall')"><i>🎁</i><b>{{ t('mall') }}</b><small>{{ lang==='zh'?'用积分兑换权益':'Redeem rewards with points' }}</small></button><button @click="go('/user/ranking')"><i>🏅</i><b>{{ t('ranking') }}</b><small>{{ lang==='zh'?'查看你的健身排名':'Check your fitness rank' }}</small></button></div>
          <section class="v10-section"><div class="v10-section-head"><div><span>{{ lang==='zh'?'开启训练':'Start Training' }}</span><h2>{{ lang==='zh'?'今日推荐':'Recommended Today' }}</h2></div><button @click="go('/user/classes')">{{ lang==='zh'?'查看全部':'View All' }} →</button></div><div class="v10-rec-grid"><article v-for="c in shortClasses" :key="c.id"><div class="v10-rec-top"><span>{{ iconFor(c.name) }}</span><em :class="{booked:isCourseBooked(c)}">{{ isCourseBooked(c) ? (lang==='zh'?'已预约':'Booked') : (spotsLeft(c)<=0 ? (lang==='zh'?'已满':'Full') : (lang==='zh'?'可预约':'Available')) }}</em></div><h3>{{ pick(c,'name') }}</h3><p>👤 {{ pick(c,'coachName') }} · ⏰ {{ timeRange(c) }} · 🎟️ {{ lang==='zh'?'剩余':'Left' }} {{ spotsLeft(c) }}</p><button @click="book(c)" :disabled="isCourseBooked(c) || isBookingBusy(c) || spotsLeft(c)<=0">{{ isBookingBusy(c) ? (lang==='zh'?'处理中...':'Processing...') : (isCourseBooked(c) ? (lang==='zh'?'已预约':'Booked') : (lang==='zh'?'立即预约':'Book Now')) }}</button></article></div></section>
          <section class="v12-community-section"><div class="v10-section-head"><div><span>{{ lang==='zh'?'社区精选':'Community Picks' }}</span><h2>{{ lang==='zh'?'社区':'Community' }}</h2></div><button @click="go('/user/social')">{{ lang==='zh'?'查看全部':'View All' }} →</button></div><div class="v12-post-grid"><article v-for="p in recentPosts" :key="p.id"><header><span>{{ avatarGlyph(p) }}</span><div><b>{{ postAuthor(p) }}</b><small>{{ postTime(p) }}</small></div></header><p>{{ p.content }}</p><footer><span>👍 {{ p.likes || 0 }}</span><span>💬 {{ p.comments || 0 }}</span></footer></article></div></section>
        </section>

        <section v-else-if="route==='/user/classes'" class="ft-page cbook-page">
          <div class="cbook-hero">
            <div class="cbook-hero-copy">
              <span class="cbook-pill">{{ t('classes') }}</span>
              <h1>{{ lang==='zh'?'课程预约中心':'Class Booking Center' }}</h1>
              <p>{{ lang==='zh'?'统一管理团课、私教课程和预约状态。':'Manage group classes, private training, and booking status in one place.' }}</p>
              <div class="cbook-hero-actions">
                <button :class="{active:classTab==='group'}" @click="classTab='group'">👥 {{ lang==='zh'?'团课':'Group Classes' }}</button>
                <button :class="{active:classTab==='private'}" @click="classTab='private'">👨‍🏫 {{ lang==='zh'?'私教课':'Private Classes' }}</button>
              </div>
            </div>
            <aside class="cbook-notes">
              <h3>{{ lang==='zh'?'预约说明':'Booking Notes' }}</h3>
              <p>{{ lang==='zh'?'成功预约会显示在我的预约中。如需取消，可在课程卡片或我的资料页面操作。':'Successful bookings will appear in My Bookings. To cancel, use the class card or the My Profile page.' }}</p>
              <b>🏆 {{ points }} {{ t('points') }}</b>
            </aside>
          </div>

          <div class="cbook-stats">
            <article><i>📌</i><b>{{ bookedClassCount }}</b><span>{{ lang==='zh'?'已预约课程':'Booked Classes' }}</span></article>
            <article><i>👥</i><b>{{ groupClassCount }}</b><span>{{ lang==='zh'?'团课':'Group Classes' }}</span></article>
            <article><i>👨‍🏫</i><b>{{ coachCount }}</b><span>{{ lang==='zh'?'私教教练':'Private Coaches' }}</span></article>
          </div>

          <div class="cbook-board">
            <div class="cbook-tabs">
              <button :class="{active:classTab==='group'}" @click="classTab='group'">👥 {{ lang==='zh'?'团课':'Group Classes' }}</button>
              <button :class="{active:classTab==='private'}" @click="classTab='private'">👨‍🏫 {{ lang==='zh'?'私教课':'Private Classes' }}</button>
            </div>

            <template v-if="classTab==='group'">
              <div class="cbook-days">
                <button v-for="d in bookingDays" :key="d.key" :class="{active:activeDay===d.key}" @click="activeDay=d.key"><span>{{ d.label }}</span><b>{{ d.date }}</b></button>
              </div>
              <div class="cbook-list">
                <article v-for="c in groupDisplayClasses" :key="c.id" class="cbook-session">
                  <div class="cbook-session-main">
                    <span class="cbook-time">{{ compactTime(c) }}</span>
                    <h3>{{ pick(c,'name') }}</h3>
                    <p>👤 {{ lang==='zh'?'教练':'Trainer' }}: {{ pick(c,'coachName') }} <em>👥 {{ Math.max(1, Number(c.bookedCount||0)) }}/{{ c.capacity || 25 }}</em></p>
                  </div>
                  <div class="cbook-status" :class="{booked:isCourseBooked(c)}">{{ isCourseBooked(c) ? (lang==='zh'?'已预约':'Booked') : (lang==='zh'?'可预约':'Available') }}</div>
                  <button :class="['cbook-book',{cancel:isCourseBooked(c)}]" @click="toggleClassBooking(c)" :disabled="isBookingBusy(c)">{{ isBookingBusy(c) ? (lang==='zh'?'处理中...':'Processing...') : (isCourseBooked(c) ? (lang==='zh'?'取消预约':'Cancel Booking') : (lang==='zh'?'立即预约':'Book Now')) }}</button>
                </article>
              </div>
            </template>

            <template v-else>
              <div class="cbook-coaches">
                <article v-for="coach in privateCoaches" :key="coach.id" class="cbook-coach-card">
                  <div class="cbook-coach-avatar">{{ coach.avatar || iconFor(coach.name) }}</div>
                  <h3>{{ pick(coach,'name') }}</h3>
                  <div class="cbook-tags"><span v-for="tag in coachTags(coach)" :key="tag">{{ tag }}</span></div>
                  <p>⭐ {{ coach.rating || '5' }} <em>📊 {{ coach.experienceYears || 5 }} {{ lang==='zh'?'年经验':'years experience' }}</em></p>
                  <button @click="openCoachCourses(coach)">{{ lang==='zh'?'查看课程':'View Courses' }}</button>
                </article>
              </div>
            </template>
          </div>

          <div v-if="selectedCoach" class="cbook-modal-shade" @click.self="selectedCoach=null">
            <section class="cbook-modal">
              <button class="cbook-close" @click="selectedCoach=null">×</button>
              <header>
                <div class="cbook-modal-avatar">{{ selectedCoach.avatar || iconFor(selectedCoach.name) }}</div>
                <div>
                  <h2>{{ pick(selectedCoach,'name') }}</h2>
                  <div class="cbook-tags modal-tags"><span v-for="tag in coachTags(selectedCoach)" :key="tag">{{ tag }}</span></div>
                  <p>⭐ {{ selectedCoach.rating || '5' }} <em>📊 {{ selectedCoach.experienceYears || 5 }} {{ lang==='zh'?'年经验':'years experience' }}</em> <em>💰 ¥{{ Number(selectedCoach.hourlyRate||400).toFixed(0) }}/hour</em></p>
                </div>
              </header>
              <h3 class="cbook-modal-title">{{ lang==='zh'?'可预约课程':'Available Courses' }}</h3>
              <div class="cbook-private-list">
                <article v-for="pc in selectedCoachCourses" :key="pc.id">
                  <div><b>{{ compactTime(pc) }}</b><h4>{{ pick(pc,'name') }}</h4><p>{{ pc.duration || (lang==='zh'?'60分钟':'60 min') }}</p></div>
                  <button :class="{cancel:isCourseBooked(pc)}" @click="togglePrivateBooking(pc)" :disabled="isBookingBusy(pc)">{{ isBookingBusy(pc) ? (lang==='zh'?'处理中...':'Processing...') : (isCourseBooked(pc) ? (lang==='zh'?'取消预约':'Cancel Booking') : (lang==='zh'?'预约':'Book')) }}</button>
                </article>
              </div>
            </section>
          </div>
        </section>

        <section v-else-if="route==='/user/profile'" class="ft-page profile-center-page">
          <div class="pc-hero">
            <div class="pc-user-block">
              <div class="pc-avatar">👤</div>
              <div>
                <h1>{{ userHandle }}</h1>
                <span class="pc-vip">{{ isVip ? (lang==='zh'?'VIP 会员':'VIP Member') : (lang==='zh'?'普通会员':'Normal Member') }}</span>
                <p>{{ lang==='zh'?'到期日期':'Expiry date' }} {{ membershipEndDate || '2025-12-31' }}</p>
              </div>
            </div>
            <div class="pc-hero-stats">
              <article><b>{{ dash.streak || 1 }}</b><span>{{ lang==='zh'?'连续打卡天数':'Streak Days' }}</span></article>
              <article><b>{{ dash.badges || 1 }}</b><span>{{ lang==='zh'?'获得勋章':'Badges Earned' }}</span></article>
              <article><b>{{ calendar.filter(d=>d.done).length || 13 }}</b><span>{{ lang==='zh'?'总打卡次数':'Total Check-ins' }}</span></article>
            </div>
          </div>
          <section class="pc-shell">
            <div class="pc-tabs">
              <button :class="{on:profileTab==='basic'}" @click="profileTab='basic'">{{ lang==='zh'?'基本信息':'Basic Info' }}</button>
              <button :class="{on:profileTab==='bookings'}" @click="profileTab='bookings'">{{ lang==='zh'?'我的预约':'My Bookings' }}</button>
              <button :class="{on:profileTab==='complaints'}" @click="profileTab='complaints'">{{ lang==='zh'?'投诉建议':'Feedback' }}</button>
              <button :class="{on:profileTab==='points'}" @click="profileTab='points'">{{ lang==='zh'?'积分':'Points' }}</button>
            </div>
            <div v-if="profileTab==='basic'" class="pc-panel pc-basic">
              <h2>{{ lang==='zh'?'基本信息':'Basic Information' }}</h2>
              <div class="pc-form-grid">
                <label>{{ lang==='zh'?'用户名':'Username' }}<input :value="userHandle" disabled /></label>
                <label>{{ lang==='zh'?'姓名':'Name' }}<input v-model="profile.name" /></label>
                <label>{{ lang==='zh'?'邮箱':'Email' }}<input v-model="profile.email" /></label>
                <label>{{ lang==='zh'?'手机号':'Phone' }}<input v-model="profile.phone" /></label>
                <label>{{ lang==='zh'?'性别':'Gender' }}<select v-model="profile.gender"><option value="male">{{ lang==='zh'?'男':'Male' }}</option><option value="female">{{ lang==='zh'?'女':'Female' }}</option></select></label>
                <label>{{ lang==='zh'?'生日':'Birthday' }}<input type="date" v-model="profile.birthday" /></label>
                <label class="wide">{{ lang==='zh'?'地址':'Address' }}<input v-model="profile.address" /></label>
                <label class="wide">{{ lang==='zh'?'健康状况 / 运动禁忌':'Health notes / exercise restrictions' }}<textarea v-model="profile.medicalNotes" :placeholder="lang==='zh'?'如有慢性病、旧伤或医嘱运动禁忌请填写，便于教练查看（可留空）':'Optional notes for coaches, injuries or restrictions'"></textarea></label>
              </div>
              <div class="pc-qr-card">
                <h3>{{ lang==='zh'?'健身房入场码（人流统计）':'Gym entry code' }}</h3>
                <p>{{ lang==='zh'?'请在到店时向工作人员出示此码扫码，用于确认场内活跃人数；也可使用下方「门店扫码」页面录入。':'Show this code at the front desk for traffic counting.' }}</p>
                <div class="pc-qr" aria-label="QR code"><i v-for="n in 81" :key="n" :class="{black: [1,2,3,4,5,7,9,10,13,15,18,19,21,24,25,27,28,31,34,36,38,40,42,44,45,47,50,52,55,57,58,60,62,65,67,68,70,73,75,76,77,78,79,81].includes(n)}"></i></div>
                <a @click="go('/gym-checkin-scan')">{{ lang==='zh'?'门店扫码确认页（前台）':'Front desk scan page' }}</a>
              </div>
              <button class="pc-save" @click="saveProfile">{{ lang==='zh'?'保存修改':'Save Changes' }}</button>
            </div>
            <div v-else-if="profileTab==='bookings'" class="pc-panel pc-bookings">
              <div class="pc-section-top"><div><h2>{{ lang==='zh'?'我的预约':'My Bookings' }}</h2><p>{{ lang==='zh'?'这里仅显示你已经预约的课程，可进行查看和取消。新的课程预约请前往导航栏的课程预约页面。':'Only confirmed bookings are shown here. Go to Class Booking for new sessions.' }}</p></div><button @click="go('/user/classes')">{{ lang==='zh'?'去预约课程':'Book Classes' }}</button></div>
              <div class="pc-days"><button v-for="d in bookingDays" :key="d.key" :class="{on:activeDay===d.key}" @click="activeDay=d.key"><b>{{ d.label }}</b><span>{{ d.date }}</span></button></div>
              <div class="pc-booking-list">
                <article v-for="b in filteredBookings" :key="b.id"><div><span>{{ compactTime(b.classes||b) }}</span><h3>{{ pick(b.classes||b,'name') || (lang==='zh'?'已预约课程':'Booked Class') }}</h3><p>👤 {{ pick(b.classes||b,'coachName') || '张教练' }} · 📍 {{ pick(b.classes||b,'location') || 'A区教室' }}</p></div><button @click="toggleClassBooking(b.classes||b)" :disabled="isBookingBusy(b.classes||b)">{{ isBookingBusy(b.classes||b) ? (lang==='zh'?'处理中...':'Processing...') : (lang==='zh'?'取消预约':'Cancel Booking') }}</button></article>
                <div v-if="!filteredBookings.length" class="pc-empty">{{ lang==='zh'?'当天暂无已预约课程':'No booked classes on this day' }}</div>
              </div>
            </div>
            <div v-else-if="profileTab==='complaints'" class="pc-panel pc-complaints">
              <h2>{{ lang==='zh'?'投诉建议':'Feedback' }}</h2>
              <label>{{ lang==='zh'?'投诉类型':'Type' }}<select v-model="complaintType"><option value="course">{{ lang==='zh'?'课程问题':'Course issue' }}</option><option value="coach">{{ lang==='zh'?'教练问题':'Coach issue' }}</option><option value="facility">{{ lang==='zh'?'场地问题':'Facility issue' }}</option><option value="other">{{ lang==='zh'?'其他问题':'Other' }}</option></select></label>
              <textarea v-model="complaintContent" :placeholder="lang==='zh'?'请输入投诉或建议内容…':'Please enter your feedback…'"></textarea>
              <button class="pc-danger" @click="submitComplaint">{{ lang==='zh'?'提交投诉':'Submit Feedback' }}</button>
              <hr />
              <h2>{{ lang==='zh'?'投诉记录':'Feedback Records' }}</h2>
              <article v-for="c in userComplaints" :key="c.id" class="pc-record"><div><span>{{ complaintLabel(c.type) }}</span><em>{{ c.status==='PENDING' ? (lang==='zh'?'待处理':'Pending') : (lang==='zh'?'已处理':'Handled') }}</em></div><p>{{ c.content }}</p><small>{{ prettyDate(c.createdAt) }}</small></article>
              <article v-if="!userComplaints.length" class="pc-record"><div><span>{{ lang==='zh'?'课程问题':'Course issue' }}</span><em>{{ lang==='zh'?'待处理':'Pending' }}</em></div><p>{{ lang==='zh'?'希望力量训练课程中增加动作分解时间。':'I hope the strength class can include more movement breakdowns.' }}</p><small>{{ prettyDate(new Date().toISOString()) }}</small></article>
            </div>
            <div v-else class="pc-panel pc-points">
              <h2>{{ lang==='zh'?'我的积分':'My Points' }}</h2>
              <div class="pc-big-points"><b>{{ points }}</b><span>{{ t('points') }}</span></div>
              <button @click="go('/user/points-mall')">🎁 {{ lang==='zh'?'前往积分商城':'Go to Points Mall' }}</button>
            </div>
          </section>
        </section>

        <section v-else-if="route==='/user/points-mall'" class="ft-page mall-v6-page">
          <section class="mall-v6-hero">
            <div class="mall-v6-gift">🎁</div>
            <div class="mall-v6-score"><h2>{{ lang==='zh'?'我的V金':'My V Coins' }}</h2><b>{{ points }}</b><p>{{ lang==='zh'?'累计获得 2680 积分':'2680 lifetime points earned' }}</p></div>
            <button class="mall-v6-earn" @click="go('/user/checkin')">👆 {{ lang==='zh'?'去签到赚积分':'Go check in to earn points' }}</button>
          </section>
          <nav class="mall-v6-tabs">
            <button v-for="tab in mallTabs" :key="tab.key" :class="{on:mallFilter===tab.key}" @click="mallFilter=tab.key"><span>{{ tab.icon }}</span>{{ tab.label }}</button>
          </nav>
          <section class="mall-v6-grid">
            <article v-for="p in shownGoods" :key="p.id" :class="['mall-v6-card',{vip: isVipGood(p)}]">
              <em v-if="isVipGood(p)">{{ lang==='zh'?'VIP专属':'VIP' }}</em>
              <div class="mall-v6-icon">{{ iconFor(p.icon || p.imageKey || p.name) }}</div>
              <h3>{{ pick(p,'name') }}</h3>
              <p>{{ pick(p,'description') }}</p>
              <small>{{ lang==='zh'?'库存':'Stock' }}: {{ p.stockQuantity || p.stock || 0 }}</small>
              <small>{{ lang==='zh'?'已售':'Sold' }}: {{ p.soldCount || 0 }}</small>
              <div class="mall-v6-cost"><b>💎 {{ p.pointsCost || p.points || 100 }}</b><button @click="redeem(p)" :disabled="redeemingId===p.id">{{ redeemingId===p.id ? (lang==='zh'?'兑换中...':'Redeeming...') : (lang==='zh'?'立即兑换':'Redeem Now') }}</button></div>
            </article>
          </section>
          <section class="mall-v6-records">
            <h2>📦 {{ lang==='zh'?'我的兑换记录':'My Redemption Records' }}</h2>
            <article v-if="!exchanges.length" class="mall-v6-empty">{{ lang==='zh'?'还没有兑换记录哦~':'No redemption records yet' }}</article>
            <article v-for="e in exchanges" :key="e.id" class="mall-v6-record"><span>🎁</span><b>{{ lang==='en' && e.productNameEn ? e.productNameEn : e.productName }}</b><em>{{ lang==='zh'?'已完成':'Completed' }}</em><strong>-{{ e.pointsCost }} {{ t('points') }}</strong></article>
          </section>
        </section>

        <section v-else-if="route==='/user/social'" class="ft-page social-v19">
          <div class="social-tabs"><button :class="{on:socialSort==='hot'}" @click="socialSort='hot'">🔥 {{ lang==='zh'?'热门':'Hot' }}</button><button :class="{on:socialSort==='latest'}" @click="socialSort='latest'">⏱️ {{ lang==='zh'?'最新':'Latest' }}</button></div>
          <div class="social-filter"><button :class="{on:socialFilter==='all'}" @click="socialFilter='all'">{{ lang==='zh'?'全部':'All' }}</button><button :class="{on:socialFilter==='share'}" @click="socialFilter='share'">📤 {{ lang==='zh'?'分享':'Share' }}</button><button :class="{on:socialFilter==='help'}" @click="socialFilter='help'">❓ {{ lang==='zh'?'求助':'Help' }}</button><button :class="{on:socialFilter==='team'}" @click="socialFilter='team'">👥 {{ lang==='zh'?'组队':'Team up' }}</button><button :class="{on:socialFilter==='interaction'}" @click="socialFilter='interaction'">💬 {{ lang==='zh'?'互动':'Interaction' }}</button></div>
          <div class="social-actions"><button class="make" @click="openTeamDialog">👥 {{ lang==='zh'?'创建组队':'Create Team' }}</button><button class="post" @click="openPostDialog">📝 {{ lang==='zh'?'发布动态':'Post Update' }}</button></div>
          <section class="social-ads"><article v-for="ad in socialAds" :key="ad.title"><h3>{{ ad.title }}</h3><p>{{ ad.description }}</p></article></section>
          <section class="social-section-title">📢 {{ lang==='zh'?'组队活动':'Team Activities' }}</section>
          <section class="team-v19-grid">
            <article v-for="team in sortedTeams" :key="team.id" class="team-v19-card">
              <div class="team-v19-head"><h3>{{ team.title }}</h3><em>{{ team.category || (lang==='zh'?'健身':'Fitness') }}</em></div>
              <p>{{ team.description }}</p>
              <div class="team-v19-meta"><span>📍 {{ team.location || (lang==='zh'?'健身中心':'Gym Center') }}</span><span>⏱️ {{ team.meetTime || (lang==='zh'?'每周':'Weekly') }}</span><span>👥 {{ team.currentMembers || team.members }}/{{ team.maxMembers || team.capacity }}</span></div>
              <small>{{ lang==='zh'?'发起者':'Creator' }}: {{ team.creatorName || team.creator || 'fitness_pro' }}</small>
              <button :class="teamButtonClass(team)" @click="toggleTeam(team)">{{ teamButtonText(team) }}</button>
            </article>
          </section>
          <section class="social-section-title">💬 {{ lang==='zh'?'动态广场':'Activity Feed' }}</section>
          <div class="social-sortbar">{{ socialSortText }} · {{ lang==='zh'?'分类':'Category' }}：{{ socialFilterLabel }} · {{ lang==='zh'?'共':'Total' }} {{ filteredPosts.length }} {{ lang==='zh'?'条':'posts' }}</div>
          <section class="feed-v19">
            <article v-for="p in filteredPosts" :key="p.id" class="feed-v19-card">
              <header><span class="feed-avatar">{{ avatarGlyph(p) }}</span><div><b>{{ p.author || p.username }}</b><small>{{ prettyDate(p.createdAt) }}</small></div><em>{{ p.category || 'share' }}</em><button :class="['pin-flag',{pinned:p.pinned}]" @click="togglePin(p)">🚩</button></header>
              <p>{{ p.content }}</p>
              <div v-if="p.replies && p.replies.length" class="feed-replies">
                <article v-for="r in p.replies" :key="r.id"><b>{{ r.author || r.username }}</b><p>{{ r.content }}</p></article>
              </div>
              <footer><button :class="{liked:p.liked || likedPosts.includes(p.id)}" @click="like(p)">👍 {{ p.likes || 0 }}</button><button @click="openReplyDialog(p)">💬 {{ p.comments || 0 }}</button></footer>
            </article>
          </section>
        </section>

        <section v-else-if="route==='/user/checkin'" class="ft-page checkin-v19">
          <section class="checkin-v19-card">
            <div class="checkin-v19-top"><div><h1>{{ checkinDateTitle }}</h1><p>{{ checkinWeekday }}</p><strong>🔥 {{ lang==='zh'?'连续打卡':'Streak' }} {{ dash.streak || 1 }} {{ lang==='zh'?'天':'day(s)' }}</strong></div><button :class="{done:checkedToday}" @click="doCheckin">🎉 {{ checkedToday ? (lang==='zh'?'今日已打卡':'Checked in today') : (lang==='zh'?'立即打卡':'Check in now') }}</button></div>
            <hr />
            <h2>🧮 {{ lang==='zh'?'本月打卡记录':'Monthly Check-in Record' }}</h2>
            <section class="checkin-v19-calendar"><div v-for="d in calendar" :key="d.n" :class="{done:d.done,today:d.today,future:d.future}"><b>{{ d.n }}</b><span v-if="d.done">✓</span></div></section>
            <h2>🏆 {{ lang==='zh'?'我的勋章':'My Badges' }}</h2>
            <section class="checkin-v19-badges"><article v-for="b in badgeV19" :key="b.name" :class="{locked:!b.unlocked}"><div>{{ b.icon }}</div><h3>{{ b.name }}</h3><p>{{ b.desc }}</p></article></section>
            <h2>🎁 {{ lang==='zh'?'打卡奖励':'Check-in Rewards' }}</h2>
            <section class="checkin-v19-rewards"><article><div><b>{{ lang==='zh'?'每日打卡':'Daily check-in' }}</b><p>{{ lang==='zh'?'每日首次打卡奖励':'Daily first check-in reward' }}</p></div><strong>+10 {{ t('points') }}</strong></article><article><div><b>{{ lang==='zh'?'连续7天':'7-day streak' }}</b><p>{{ lang==='zh'?'连续打卡7天奖励':'Reward for 7-day streak' }}</p></div><strong>+50 {{ t('points') }}</strong></article><article><div><b>{{ lang==='zh'?'连续30天':'30-day streak' }}</b><p>{{ lang==='zh'?'连续打卡30天奖励':'Reward for 30-day streak' }}</p></div><strong>+200 {{ t('points') }}</strong></article></section>
          </section>
        </section>

        <section v-else-if="route==='/user/ranking'" class="ft-page ranking-v19">
          <header class="ranking-v19-head"><h1>🏆 {{ lang==='zh'?'健身达人排行榜':'Fitness Leaderboard' }}</h1><p>{{ lang==='zh'?'按预约课程节数排行，打卡天数和积分用于同分排序':'Ranked by booked class count, check-in days and points break ties' }}</p></header>
          <nav class="ranking-v19-tabs"><button v-for="tab in rankPeriodTabs" :key="tab.key" :class="{on:rankingPeriod===tab.key}" @click="switchRanking(tab.key)">{{ tab.icon }} {{ tab.label }}</button></nav>
          <section class="ranking-v19-podium">
            <article v-if="rankPodium.second" class="second"><div>{{ rankAvatar(rankPodium.second) }}</div><h3>{{ rankPodium.second.name }}</h3><b>{{ rankPodium.second.score || rankPodium.second.totalClasses }}{{ lang==='zh'?'节':' classes' }}</b><p>{{ rankPodium.second.days || rankPodium.second.checkinDays }}{{ lang==='zh'?'天打卡':' days' }} · {{ rankPodium.second.points || rankPodium.second.availablePoints }} {{ t('points') }}</p><em>2nd</em></article>
            <article v-if="rankPodium.first" class="first"><i>👑</i><div>{{ rankAvatar(rankPodium.first) }}</div><h3>{{ rankPodium.first.name }}</h3><b>{{ rankPodium.first.score || rankPodium.first.totalClasses }}{{ lang==='zh'?'节':' classes' }}</b><p>{{ rankPodium.first.days || rankPodium.first.checkinDays }}{{ lang==='zh'?'天打卡':' days' }} · {{ rankPodium.first.points || rankPodium.first.availablePoints }} {{ t('points') }}</p><em>1st</em></article>
            <article v-if="rankPodium.third" class="third"><div>{{ rankAvatar(rankPodium.third) }}</div><h3>{{ rankPodium.third.name }}</h3><b>{{ rankPodium.third.score || rankPodium.third.totalClasses }}{{ lang==='zh'?'节':' classes' }}</b><p>{{ rankPodium.third.days || rankPodium.third.checkinDays }}{{ lang==='zh'?'天打卡':' days' }} · {{ rankPodium.third.points || rankPodium.third.availablePoints }} {{ t('points') }}</p><em>3rd</em></article>
          </section>
          <section class="ranking-v19-list"><article v-for="(r,i) in rankList.slice(3)" :key="r.id"><span>{{ i+4 }}</span><i>{{ rankAvatar(r) }}</i><div><b>{{ r.name }}</b><small>{{ r.days || r.checkinDays }}{{ lang==='zh'?'天打卡':' days' }} · {{ r.score || r.totalClasses }}{{ lang==='zh'?'节':' classes' }} · {{ r.points || r.availablePoints }} {{ t('points') }}</small></div><strong>{{ r.score || r.totalClasses }}{{ lang==='zh'?'节':' classes' }}</strong></article></section>
          <section class="ranking-v19-me"><div><h3>{{ lang==='zh'?'我的排名':'My Rank' }}</h3><span class="me-avatar">{{ avatarGlyph({author:userHandle}) }}</span><b>{{ userHandle }}</b><p>{{ lang==='zh'?'已预约':'Booked' }} {{ dash.bookings || bookedClassCount }} {{ lang==='zh'?'节':'classes' }}</p></div><strong>#4</strong><aside><i><em :style="{width:'82%'}"></em></i><small>{{ lang==='zh'?'距离上一名还差 3 节':'3 classes behind the next rank' }}</small></aside></section>
        </section>

        <section v-else-if="route==='/user/vip-payment'" class="ft-page v10-page">
          <div class="v10-vip-hero"><span>{{ lang==='zh'?'VIP 会员':'VIP Membership' }}</span><h1>{{ lang==='zh'?'开通 FitLife VIP':'Activate FitLife VIP' }}</h1><p>{{ lang==='zh'?'这是项目演示支付流程，确认支付后系统会同步更新个人资料、管理端会员管理和教练学员信息。':'This is a demo payment flow for the project. After confirming payment, the system updates your membership and syncs it with profile, admin membership management, and coach student information.' }}</p><div><b>🎟️ {{ lang==='zh'?'更好的预约体验':'Better booking experience' }}</b><b>👑 {{ lang==='zh'?'专属商品':'Access VIP-exclusive items' }}</b><b>⭐ {{ lang==='zh'?'VIP身份':'Receive VIP identity' }}</b></div></div>
          <div class="v10-vip-pay"><section><h2>{{ lang==='zh'?'选择套餐':'Choose a plan' }}</h2><div class="v10-vip-cards"><article v-for="plan in vipPlans" :key="plan.id" :class="{hot:plan.hot,chosen:selectedPlan===plan.id}" @click="selectedPlan=plan.id"><span>{{ plan.hot ? (lang==='zh'?'推荐':'Recommended') : (plan.id==='family'?(lang==='zh'?'超值':'Best value'):(lang==='zh'?'灵活':'Flexible')) }}</span><h3>{{ plan.name }}</h3><b>¥{{ plan.price }}</b><p>{{ plan.desc }}</p></article></div></section><aside><h2>{{ lang==='zh'?'支付信息':'Payment information' }}</h2><label :class="{chosen:selectedPayment==='wechat'}" @click="selectedPayment='wechat'">💚 WeChat Pay</label><label :class="{chosen:selectedPayment==='alipay'}" @click="selectedPayment='alipay'">💙 Alipay</label><label :class="{chosen:selectedPayment==='card'}" @click="selectedPayment='card'">💳 Bank Card</label><button @click="openPaymentModal">{{ lang==='zh'?'确认支付':'Confirm Payment' }}</button></aside></div>
        </section>
        <div v-if="showPayModal" class="pay-modal-backdrop" @click.self="showPayModal=false">
          <section class="pay-modal-card">
            <button class="pay-modal-close" @click="showPayModal=false">×</button>
            <h2>{{ paymentTitle }}</h2>
            <p>{{ lang==='zh'?'请使用对应支付方式扫描二维码完成支付':'Scan the QR code with the selected payment method' }}</p>
            <div class="pay-qr"><i v-for="n in 121" :key="n" :class="{black: qrPattern(n)}"></i></div>
            <small>{{ lang==='zh'?'如果已支付，请关闭':'If paid, please close' }}</small>
            <button class="pay-finish" :disabled="vipProcessing" @click="finishVipPayment">{{ vipProcessing ? (lang==='zh'?'处理中...':'Processing...') : (lang==='zh'?'关闭并完成开通':'Close and activate VIP') }}</button>
          </section>
        </div>
      </main>
    </template>

    <template v-else-if="area === 'admin'">
      <div class="admin-v24-shell">
        <header class="admin-v24-head">
          <div class="admin-v24-brand"><span>👥</span><strong>FitLife Admin Login</strong></div>
          <div class="admin-v24-tools">
            <div class="admin-v24-lang"><button @click="setLang('zh')" :class="{on:lang==='zh'}">中文</button><button @click="setLang('en')" :class="{on:lang==='en'}">English</button></div>
            <b>admin</b><button class="admin-v24-logout" @click="logout">{{ t('logout') }}</button>
          </div>
        </header>
        <div class="admin-v24-body">
          <aside class="admin-v24-menu">
            <button v-for="item in adminMenu" :key="item.path" :class="{on:route===item.path}" @click="go(item.path)"><span>{{ item.icon }}</span>{{ t(item.key) }}</button>
          </aside>
          <main class="admin-v24-main">
            <section v-if="route==='/admin/dashboard'" class="admin-v24-page">
              <div class="admin-v24-title"><h1>{{ lang==='zh'?'会员管理':'Members Management' }}</h1><button @click="openUserDialog('add','member')">{{ lang==='zh'?'添加会员':'Add Member' }}</button></div>
              <div class="admin-v24-stats three">
                <article><i>👥</i><b>{{ adminStats.totalMembers }}</b><span>{{ lang==='zh'?'总会员':'Total Members' }}</span></article>
                <article><i>✅</i><b>{{ adminStats.activeMembers }}</b><span>{{ lang==='zh'?'活跃会员':'Active Members' }}</span></article>
                <article><i>📈</i><b>{{ adminStats.newMembers }}</b><span>{{ lang==='zh'?'本月新增':'New This Month' }}</span></article>
              </div>
              <div class="admin-v24-card"><AdminTable :rows="adminUsers" mode="members" :lang="lang" @edit="row=>openUserDialog('edit','member',row)" @remove="deleteUser" /></div>
            </section>
            <section v-else-if="route==='/admin/users'" class="admin-v24-page">
              <div class="admin-v24-title"><h1>{{ lang==='zh'?'用户管理':'User Management' }}</h1><button @click="openUserDialog('add','user')">{{ lang==='zh'?'添加用户':'Add User' }}</button></div>
              <div class="admin-v24-card"><AdminTable :rows="adminUsers" mode="users" :lang="lang" @edit="row=>openUserDialog('edit','user',row)" @remove="deleteUser" /></div>
            </section>
            <section v-else-if="route==='/admin/classes'" class="admin-v24-page">
              <div class="admin-v24-title"><h1>{{ lang==='zh'?'课程管理':'Class Management' }}</h1><button @click="openClassDialog('add')">{{ lang==='zh'?'添加课程':'Add Class' }}</button></div>
              <div class="admin-v25-tabs"><button :class="{on:adminClassTab==='private'}" @click="adminClassTab='private'">{{ lang==='zh'?'私教 / 小团课':'Private / Small group' }}</button><button :class="{on:adminClassTab==='group'}" @click="adminClassTab='group'">{{ lang==='zh'?'团课':'Group class' }}</button></div>
              <div class="admin-v24-card admin-v24-table"><table><thead><tr><th>ID</th><th>{{ lang==='zh'?'课程名称':'Course name' }}</th><th>{{ lang==='zh'?'教练':'Coach' }}</th><th>{{ lang==='zh'?'时间':'Time' }}</th><th>{{ lang==='zh'?'容量':'Capacity' }}</th><th>{{ lang==='zh'?'场地':'Location' }}</th><th>{{ lang==='zh'?'状态':'Status' }}</th><th>{{ lang==='zh'?'操作':'Actions' }}</th></tr></thead><tbody><tr v-for="c in adminClassRows" :key="c.id"><td>{{ c.id }}</td><td>{{ pick(c,'name') }}</td><td>{{ pick(c,'coachName') }}</td><td>{{ adminTimeRange(c) }}</td><td>{{ c.bookedCount || 0 }} / {{ c.capacity || 1 }}</td><td>{{ pick(c,'location') }}</td><td><span :class="['ft-status', classFull(c)?'full':'on']">{{ classFull(c) ? 'Full' : statusLabel(c.status) }}</span></td><td><button @click="openClassDialog('edit',c)">{{ lang==='zh'?'编辑':'Edit' }}</button><button class="danger" @click="adminDeleteClass(c)">{{ lang==='zh'?'删除':'Delete' }}</button></td></tr></tbody></table></div>
            </section>
            <section v-else-if="route==='/admin/products'" class="admin-v24-page">
              <div class="admin-v24-title"><div><h1>{{ lang==='zh'?'商品管理':'Product Management' }}</h1><p class="admin-v34-sub">{{ goods.length }} {{ lang==='zh'?'件商品已连接数据库':'products in database' }}</p></div><button @click="openProductDialog('add')">{{ lang==='zh'?'添加商品':'Add Product' }}</button></div>
              <div class="admin-v34-tools"><input v-model="adminProductSearch" :placeholder="lang==='zh'?'搜索商品...':'Search products...'" /><select v-model="adminProductCategory"><option value="all">{{ lang==='zh'?'全部分类':'All categories' }}</option><option v-for="cat in adminProductCategories" :key="cat" :value="cat">{{ productCategoryLabel(cat) }}</option></select><button @click="loadAdmin">Refresh</button></div>
              <div class="admin-v24-products admin-v34-products"><article v-for="p in filteredAdminProducts" :key="p.id"><div>{{ iconFor(p.imageKey || p.icon || p.name) }}</div><h3>{{ pick(p,'name') }}</h3><p>{{ pick(p,'description') || (lang==='zh'?'暂无描述':'No description') }}</p><span>{{ productCategoryLabel(p.category || 'Coupons') }} <em class="admin-v34-active">{{ p.active===false?(lang==='zh'?'未启用':'Inactive'):(lang==='zh'?'启用':'Active') }}</em></span><b>💎 {{ p.pointsCost || p.points || 0 }} {{ lang==='zh'?'积分':'Points' }}</b><span>{{ lang==='zh'?'库存':'Stock' }} {{ p.stockQuantity || p.stock || 0 }}</span><footer><button @click="openProductDialog('edit',p)">{{ lang==='zh'?'编辑':'Edit' }}</button><button class="danger" @click="adminDeleteProduct(p)">{{ lang==='zh'?'删除':'Delete' }}</button></footer></article></div>
            </section>
            <section v-else-if="route==='/admin/community'" class="admin-v24-page admin-v34-community">
              <div class="admin-v24-title"><h1>{{ lang==='zh'?'社区管理':'Community Management' }}</h1><div class="admin-v34-switch"><button :class="{on:adminCommunityTab==='posts'}" @click="adminCommunityTab='posts'">Posts</button><button :class="{on:adminCommunityTab==='reports'}" @click="adminCommunityTab='reports'">Reports</button></div></div>
              <div v-if="adminCommunityTab==='posts'" class="admin-v34-posts"><article v-for="p in posts" :key="p.id" class="admin-v34-post"><div class="bubble">💬</div><div class="body"><header><div><b>{{ p.author || p.username }}</b><small>{{ prettyDate(p.createdAt) }}</small></div><em>{{ adminPostCategoryLabel(p.category) }}</em></header><p>{{ p.content }}</p><div class="meta">👍 {{ p.likes || 0 }}　💬 {{ p.comments || (p.replies&&p.replies.length) || 0 }}</div><footer><button @click="openPostDialog(p)">Edit</button><button class="danger" @click="deletePost(p)">Delete</button></footer></div></article></div>
              <div v-else class="admin-v34-reports"><article v-for="r in adminReports" :key="r.id" class="admin-v34-report"><header><div><b>Report No.{{ r.id }}</b><small>{{ prettyDate(r.createdAt) }}</small></div><span :class="String(r.status).toUpperCase().includes('PENDING')?'pending':'resolved'">{{ reportStatusLabel(r.status) }}</span></header><section><p><b>Reporter:</b>{{ r.reporter }}</p><p><b>Reported post:</b>{{ r.postContent || r.content || r.reportedContent || '' }}</p><p><b>Reason:</b><em>{{ r.reason || r.type }}</em></p></section><footer><button :disabled="!String(r.status).toUpperCase().includes('PENDING')" class="handle" @click="processAdminReport(r)">Handle</button><button :disabled="!String(r.status).toUpperCase().includes('PENDING')" class="dismiss" @click="ignoreAdminReport(r)">Dismiss</button></footer></article></div>
            </section>
          </main>
        </div>
        <div v-if="adminDialog" class="admin-v25-modal-bg" @click.self="closeAdminDialog">
          <section :class="['admin-v25-modal', adminDialog && adminDialog.kind==='product' ? 'wide' : '']">
            <h2>{{ adminDialogTitle }}</h2>
            <p v-if="adminDialog.mode==='edit'" class="admin-v25-subtitle">{{ adminForm.username }}</p>

            <template v-if="adminDialog.kind==='member' || adminDialog.kind==='user'">
              <label v-if="adminDialog.mode==='add'">Username<input v-model="adminForm.username" /></label>
              <label v-if="adminDialog.mode==='add'">Password<input type="password" v-model="adminForm.password" /></label>
              <label>Name<input v-model="adminForm.name" /></label>
              <label>Email<input v-model="adminForm.email" /></label>
              <label>Phone<input v-model="adminForm.phone" /></label>
              <label v-if="adminDialog.mode==='add'">Member Level<select v-model="adminForm.membershipType"><option value="标准会员">Normal Member</option><option value="VIP会员">VIP Member</option></select></label>
              <label v-if="adminDialog.kind==='user' && adminDialog.mode==='edit'">Role<select v-model="adminForm.role"><option value="USER">Member</option><option value="ADMIN">Administrator</option></select></label>
              <label v-if="adminDialog.kind==='user' && adminDialog.mode==='edit'">New password (leave blank to keep current)<input type="password" v-model="adminForm.newPassword" /></label>
              <label class="admin-v25-check"><input type="checkbox" v-model="adminForm.active" /> Account active</label>
            </template>

            <template v-else-if="adminDialog.kind==='class'">
              <label>Course name<input v-model="adminForm.name" /></label>
              <label v-if="adminDialog.mode==='add'">English course name<input v-model="adminForm.nameEn" /></label>
              <label>Coach<select v-model="adminForm.coachId"><option value="">Select a coach</option><option v-for="c in coaches" :key="c.id" :value="String(c.id)">{{ c.name }} ({{ c.username || ('coach_'+c.id) }})</option></select></label>
              <label>Max capacity<input type="number" min="1" v-model.number="adminForm.capacity" /></label>
              <label>Start<input type="datetime-local" v-model="adminForm.startTime" /></label>
              <label>End<input type="datetime-local" v-model="adminForm.endTime" /></label>
              <label>Location<input v-model="adminForm.location" /></label>
              <label v-if="adminDialog.mode==='add'">English location<input v-model="adminForm.locationEn" /></label>
              <label>Status<select v-model="adminForm.status"><option value="ACTIVE">Active</option><option value="PENDING">Pending review</option><option value="INACTIVE">Inactive</option></select></label>
            </template>

            <template v-else-if="adminDialog.kind==='product'">
              <div class="admin-v34-two"><label>Product name<input v-model="adminForm.name" /></label><label>English product name<input v-model="adminForm.nameEn" placeholder="e.g. Sports Water Bottle" /></label></div>
              <div class="admin-v34-two"><label>Category<select v-model="adminForm.category"><option v-for="opt in productCategoryOptions" :key="opt.value" :value="opt.value">{{ lang==='zh'?opt.zh:opt.en }}</option></select></label><label>Points cost<input type="number" min="0" v-model.number="adminForm.pointsCost" /></label></div>
              <label>Stock quantity<input type="number" min="0" v-model.number="adminForm.stockQuantity" /></label>
              <label>Description<textarea v-model="adminForm.description"></textarea></label>
              <label>English description<textarea v-model="adminForm.descriptionEn" placeholder="e.g. Large-capacity sports bottle"></textarea></label>
              <label>Product icon<input v-model="adminForm.imageKey" /></label>
              <label class="admin-v25-check"><input type="checkbox" v-model="adminForm.active" /> Product active</label>
            </template>

            <template v-else-if="adminDialog.kind==='post'">
              <p class="admin-v25-subtitle">ID: {{ adminForm.id }}</p>
              <label>Category<select v-model="adminForm.category"><option value="share">📥 Share</option><option value="help">❓ Help</option><option value="team">👥 Team</option><option value="chat">💬 Chat</option></select></label>
              <label>Content<textarea v-model="adminForm.content"></textarea></label>
              <label class="admin-v25-check"><input type="checkbox" v-model="adminForm.visible" /> Visible on community feed</label>
            </template>

            <div class="admin-v25-actions"><button class="outline" @click="closeAdminDialog">Cancel</button><button @click="saveAdminDialog">Save</button></div>
          </section>
        </div>
      </div>
    </template>

    <template v-else-if="area === 'coach'">
      <div class="coach-v36-shell">
        <header class="coach-v36-head">
          <div class="coach-v36-inner">
            <div class="coach-v36-brand"><span>👨‍🏫</span><strong>FitLife Coach Login</strong></div>
            <nav class="coach-v36-nav">
              <button v-for="item in coachMenu" :key="item.path" :class="{on:route===item.path}" @click="go(item.path)">{{ coachNavLabel(item) }}</button>
            </nav>
            <div class="coach-v36-tools">
              <div class="coach-v36-lang"><button @click="setLang('zh')" :class="{on:lang==='zh'}">中文</button><button @click="setLang('en')" :class="{on:lang==='en'}">English</button></div>
              <b>{{ userHandle || 'coach_1' }}</b>
              <button @click="logout">Logout</button>
            </div>
          </div>
        </header>
        <main class="coach-v36-main">
          <section v-if="route==='/coach/dashboard'" class="coach-v36-page coach-v36-dashboard">
            <h1>🏠 {{ lang==='zh'?'Coach Dashboard':'Coach Dashboard' }}</h1>
            <p>{{ lang==='zh'?'Welcome back':'Welcome back' }}, {{ userHandle || 'coach_1' }}!</p>
            <div class="coach-v36-stat-row">
              <article><i>👥</i><b>{{ coachStats.activeUsers || 0 }}</b><span>{{ lang==='zh'?'Active Users in Gym':'Active Users in Gym' }}</span></article>
              <article><i>🗓️</i><b>{{ coachTodaySchedule.length }}</b><span>{{ lang==='zh'?"Today's Classes":"Today's Classes" }}</span></article>
              <article><i>🔔</i><b>{{ coachPendingCount }}</b><span>{{ lang==='zh'?'Pending Requests':'Pending Requests' }}</span></article>
            </div>
            <section class="coach-v36-schedule-card">
              <h2>📋 {{ lang==='zh'?"Today's Schedule":"Today's Schedule" }}</h2>
              <div v-if="!coachTodaySchedule.length" class="coach-v36-empty"><i>🗓️</i><p>{{ lang==='zh'?'No schedule for today':'No schedule for today' }}</p></div>
              <article v-for="c in coachTodaySchedule" :key="c.id" class="coach-v36-schedule-item">
                <div><strong>{{ coachDisplayTime(c) }}</strong><b>{{ pick(c,'name') }}</b><span>📍 {{ pick(c,'location') }} · 👥 {{ c.bookedCount||0 }}/{{ c.capacity||20 }}</span></div>
                <em :class="coachClassStatusClass(c)">{{ coachClassStatusLabel(c) }}</em>
              </article>
            </section>
          </section>

          <section v-else-if="route==='/coach/profile'" class="coach-v36-page">
            <h1>👤 {{ lang==='zh'?'Personal Center':'Personal Center' }}</h1><p>{{ lang==='zh'?'Manage your profile and notification preferences':'Manage your profile and notification preferences' }}</p>
            <div class="coach-v36-profile-grid">
              <section class="coach-v36-profile-card">
                <h2>Profile</h2>
                <div class="coach-v36-profile-head"><div class="coach-v36-avatar">{{ coachProfile.avatar || '👨‍🏫' }}</div><div><h3>{{ coachProfile.name || '张教练' }}</h3><p>Certified Coach</p></div></div>
                <dl>
                  <div><dt>Username</dt><dd>{{ coachProfile.username || 'coach_1' }}</dd></div>
                  <div><dt>Email</dt><dd>{{ coachProfile.email || 'coach1@fitlife.com' }}</dd></div>
                  <div><dt>Phone</dt><dd>{{ coachProfile.phone || '13800000001' }}</dd></div>
                  <div><dt>Experience</dt><dd>{{ coachProfile.experienceYears || 5 }} yrs</dd></div>
                  <div><dt>Certifications</dt><dd>{{ coachProfile.certifications || 'National Fitness Coach Certification, Advanced Personal Trainer Certificate' }}</dd></div>
                  <div><dt>Specialty</dt><dd>{{ coachProfile.specialty || 'Strength Training, Muscle Gain & Body Shaping, Functional Training' }}</dd></div>
                  <div><dt>Hourly rate</dt><dd>¥{{ Number(coachProfile.hourlyRate||300).toFixed(0) }}/hr</dd></div>
                  <div><dt>Rating</dt><dd>⭐ {{ coachProfile.rating || '4.6' }}</dd></div>
                </dl>
              </section>
              <section class="coach-v36-notify-card">
                <h2>Notifications</h2>
                <article v-for="n in coachNotifyRows" :key="n.key"><div><b>{{ n.title }}</b><span>{{ n.desc }}</span></div><label class="coach-v36-switch"><input type="checkbox" v-model="coachNotify[n.key]"><i></i></label></article>
                <button @click="saveCoachSettings">Save settings</button>
              </section>
            </div>
            <section class="coach-v36-crowd"><h2>Gym crowd count</h2><p>Active count is based on members who scanned or checked in within the last hour.<br>Members show the QR from Profile; staff scans it at the desk.</p><a href="#">Open staff scan page (tablet)</a></section>
            <h2 class="coach-v36-overview-title">📊 Overview</h2>
            <div class="coach-v36-overview"><article><b>{{ coachClasses.length }}</b><span>Total classes</span></article><article><b>{{ coachStudents.length }}</b><span>Total students</span></article><article><b>{{ coachProfile.rating || '4.6' }}</b><span>Avg. rating</span></article><article><b>{{ coachMonthlyCount }}</b><span>This month</span></article></div>
          </section>

          <section v-else-if="route==='/coach/classes'" class="coach-v36-page">
            <h1>📚 Class Management</h1><p>Manage your classes and students</p>
            <div class="coach-v36-tabs">
              <button :class="{on:coachSubTab==='mine'}" @click="coachSubTab='mine'">📋 My Classes</button>
              <button :class="{on:coachSubTab==='add'}" @click="coachSubTab='add'; resetCoachClassForm()">➕ Add Class</button>
              <button :class="{on:coachSubTab==='facility'}" @click="coachSubTab='facility'">🏋️ Book Facility</button>
              <button :class="{on:coachSubTab==='students'}" @click="coachSubTab='students'">👥 Students</button>
            </div>

            <section v-if="coachSubTab==='mine'" class="coach-v36-panel">
              <select class="coach-v36-select" v-model="coachClassFilter"><option value="all">All Classes</option><option value="upcoming">Upcoming</option><option value="completed">Completed</option></select>
              <div class="coach-v36-class-grid">
                <article v-for="c in filteredCoachClasses" :key="c.id" class="coach-v36-class-card">
                  <em>{{ coachDisplayTime(c) }}</em><span :class="coachClassStatusClass(c)">{{ coachClassStatusLabel(c) }}</span>
                  <h3>{{ pick(c,'name') }}</h3><p>📍 {{ pick(c,'location') }} &nbsp;&nbsp; 👥 {{ c.bookedCount||0 }}/{{ c.capacity||20 }} people</p>
                  <footer><button @click="openCoachEdit(c)">Edit</button><button class="danger" @click="coachDeleteClass(c)">Delete</button></footer>
                </article>
              </div>
            </section>

            <section v-else-if="coachSubTab==='add'" class="coach-v36-panel coach-v36-add-panel">
              <label>Class Name<input v-model="coachClassForm.name" placeholder="Enter class name"></label>
              <div class="coach-v36-form-row"><label>Date<input v-model="coachClassForm.date" type="date"></label><label>Start Time<input v-model="coachClassForm.startTime" type="time"></label><label>End Time<input v-model="coachClassForm.endTime" type="time"></label></div>
              <div class="coach-v36-form-row"><label>Location <small>Book a facility first</small><select v-model="coachClassForm.reservedBookingId" @change="chooseReservedFacility"><option value="">Select a booked facility</option><option v-for="b in availableCoachBookings" :key="b.id" :value="String(b.id)">{{ pickFacilityBooking(b) }}</option></select><small>Only confirmed facility bookings are shown here. Selecting one fills in the date and time automatically.</small></label><label>Capacity<input v-model.number="coachClassForm.capacity" type="number" min="1"></label></div>
              <label>Description<textarea v-model="coachClassForm.description" placeholder="Enter class description"></textarea></label>
              <button class="coach-v36-wide" @click="submitCoachClass">Add Class</button>
            </section>

            <section v-else-if="coachSubTab==='facility'" class="coach-v36-panel">
              <div class="coach-v36-facilities">
                <article v-for="f in coachFacilities" :key="f.id" class="coach-v36-facility-card">
                  <header><h3>{{ pick(f,'name') }}</h3><span>Available</span></header>
                  <p>{{ pick(f,'description') }}</p><p><b>Equipment:</b> {{ pick(f,'equipment') }}</p>
                  <div class="coach-v36-slot-box"><h4>Available Slots</h4><small>Tap a time chip below. The confirm button appears under the selected slot, so no browser popup is used.</small>
                    <div v-for="slot in facilitySlots(f)" :key="slot" class="coach-v37-slot-row">
                      <button type="button" :class="{picked:isFacilitySlotSelected(f,slot)}" @click="selectFacilitySlot(f,slot)">{{ slot.replace('-', ' – ') }}</button>
                      <div v-if="isFacilitySlotSelected(f,slot)" class="coach-v37-slot-confirm">
                        <span>{{ lang==='zh' ? '确认预约该时段？' : 'Confirm this slot?' }}</span>
                        <button type="button" class="confirm" @click.stop="confirmFacilitySlot(f,slot)">{{ lang==='zh' ? '确认预约' : 'Confirm' }}</button>
                        <button type="button" class="ghost" @click.stop="clearFacilitySlot">{{ lang==='zh' ? '取消' : 'Cancel' }}</button>
                      </div>
                    </div>
                  </div>
                </article>
              </div>
              <section class="coach-v36-reserved"><header><h3>My Reserved Facilities</h3><button @click="loadCoach">Refresh</button></header><article v-for="b in coachFacilityBookings" :key="b.id"><div><b>{{ pickFacilityBooking(b) }}</b><span>{{ b.usedByClass ? 'Used by class setup' : 'Available for class setup' }}</span></div><button @click="cancelFacilityBooking(b)">Delete</button></article><p v-if="!coachFacilityBookings.length">No reserved facility yet</p></section>
            </section>

            <section v-else class="coach-v36-panel coach-v36-students">
              <input v-model="coachStudentSearch" placeholder="Search by name or phone">
              <article v-for="s in visibleCoachStudents" :key="s.id || s.username"><div class="coach-v36-student-avatar">{{ avatarGlyph(s) }}</div><div><b>{{ s.name }}</b><span>{{ s.membershipType || 'Regular Member' }} &nbsp; {{ maskPhone(s.phone) }} <em v-if="s.medicalNotes">Has Disease</em> <em class="ok">Attended</em></span></div><strong>Attended</strong></article>
            </section>
          </section>

          <section v-else-if="route==='/coach/mailbox'" class="coach-v36-page">
            <h1>✉️ {{ lang==='zh'?'教练邮箱':'Coach Mailbox' }}</h1>
            <div class="coach-v24-mailbox">
              <section>
                <h2>{{ lang==='zh'?'投诉建议':'Complaints' }}</h2>
                <article v-for="c in complaints" :key="c.id">
                  <b>{{ pick(c,'title') }}</b><em>{{ c.status }}</em><p>{{ pick(c,'content') }}</p>
                  <p v-if="c.response" class="coach-v36-mail-reply">↳ {{ pick(c,'response') }}</p>
                  <button @click="openCoachComplaint(c)">{{ lang==='zh'?'处理':'Handle' }}</button>
                </article>
              </section>
              <section>
                <h2>{{ lang==='zh'?'课程评价':'Reviews' }}</h2>
                <article v-for="r in reviews" :key="r.id">
                  <b>⭐ {{ r.rating }} · {{ pick(r,'className') }}</b><em>{{ r.responded ? (lang==='zh'?'已回复':'Replied') : (lang==='zh'?'待回复':'Pending') }}</em><p>{{ pick(r,'content') }}</p>
                  <p v-if="r.response" class="coach-v36-mail-reply">↳ {{ pick(r,'response') }}</p>
                  <button @click="openCoachReview(r)">{{ lang==='zh'?'回复':'Reply' }}</button>
                </article>
              </section>
            </div>
          </section>
        </main>

        <div v-if="coachEditDialog" class="coach-v36-modal-backdrop" @click.self="coachEditDialog=null">
          <section class="coach-v36-edit-modal">
            <button class="coach-v36-close" @click="coachEditDialog=null">×</button>
            <h2>Edit Class</h2>
            <label>Class Name<input v-model="coachEditDialog.name"></label>
            <div class="coach-v36-form-row"><label>Date<input v-model="coachEditDialog.date" type="date"></label><label>Start Time<input v-model="coachEditDialog.startTime" type="time"></label></div>
            <div class="coach-v36-form-row"><label>End Time<input v-model="coachEditDialog.endTime" type="time"></label></div>
            <div class="coach-v36-form-row"><label>Location<select v-model="coachEditDialog.location"><option v-for="f in coachFacilities" :key="f.id" :value="pick(f,'name')">{{ pick(f,'name') }}</option><option :value="coachEditDialog.location">{{ coachEditDialog.location }}</option></select></label><label>Capacity<input v-model.number="coachEditDialog.capacity" type="number" min="1"></label></div>
            <footer><button class="ghost" @click="coachEditDialog=null">Cancel</button><button @click="saveCoachEdit">Save</button></footer>
          </section>
        </div>
        <div v-if="coachMailDialog" class="coach-v36-modal-backdrop" @click.self="coachMailDialog=null">
          <section class="coach-v36-edit-modal">
            <button class="coach-v36-close" @click="coachMailDialog=null">×</button>
            <h2>{{ coachMailDialog.kind==='complaint' ? (lang==='zh'?'处理投诉':'Handle Complaint') : (lang==='zh'?'回复评价':'Reply Review') }}</h2>
            <article class="coach-v36-mail-preview">
              <b>{{ coachMailDialog.kind==='complaint' ? pick(coachMailDialog.row,'title') : ('⭐ '+coachMailDialog.row.rating+' · '+pick(coachMailDialog.row,'className')) }}</b>
              <p>{{ pick(coachMailDialog.row,'content') }}</p>
            </article>
            <label>{{ lang==='zh'?'回复内容':'Reply content' }}<textarea v-model="coachMailReply" :placeholder="lang==='zh'?'请输入处理说明或回复内容':'Enter handling note or reply content'"></textarea></label>
            <footer><button class="ghost" @click="coachMailDialog=null">{{ lang==='zh'?'取消':'Cancel' }}</button><button @click="submitCoachMailReply">{{ lang==='zh'?'提交':'Submit' }}</button></footer>
          </section>
        </div>
      </div>
    </template>

    <div v-if="showTeamModal" class="clean-modal-backdrop" @click.self="showTeamModal=false">
      <section class="clean-social-modal">
        <header><h2>👥 {{ lang==='zh'?'创建组队':'Create Team' }}</h2><button @click="showTeamModal=false">×</button></header>
        <label>{{ lang==='zh'?'组队名称':'Team name' }}<input v-model="teamForm.title" :placeholder="lang==='zh'?'例如：周末晨跑小队':'e.g. Weekend running team'" /></label>
        <label>{{ lang==='zh'?'活动说明':'Description' }}<textarea v-model="teamForm.description" :placeholder="lang==='zh'?'写下集合时间、训练目标和参与要求':'Describe time, goal and requirements'"></textarea></label>
        <div class="clean-modal-grid"><label>{{ lang==='zh'?'分类':'Category' }}<select v-model="teamForm.category"><option value="running">{{ lang==='zh'?'跑步':'Running' }}</option><option value="yoga">{{ lang==='zh'?'瑜伽':'Yoga' }}</option><option value="fitness">{{ lang==='zh'?'健身 / 力量':'Fitness / Strength' }}</option><option value="swimming">{{ lang==='zh'?'游泳':'Swimming' }}</option></select></label><label>{{ lang==='zh'?'人数上限':'Capacity' }}<input type="number" min="2" v-model.number="teamForm.maxMembers" /></label></div>
        <div class="clean-modal-grid"><label>{{ lang==='zh'?'地点':'Location' }}<input v-model="teamForm.location" :placeholder="lang==='zh'?'健身中心A区':'Gym Zone A'" /></label><label>{{ lang==='zh'?'时间':'Meet time' }}<input v-model="teamForm.meetTime" :placeholder="lang==='zh'?'周六 07:00':'Sat 07:00'" /></label></div>
        <footer><button class="ghost" @click="showTeamModal=false">{{ lang==='zh'?'取消':'Cancel' }}</button><button class="primary" @click="submitTeamForm">{{ lang==='zh'?'创建组队':'Create Team' }}</button></footer>
      </section>
    </div>

    <div v-if="showPostModal" class="clean-modal-backdrop" @click.self="showPostModal=false">
      <section class="clean-social-modal post-box">
        <header><h2>📝 {{ lang==='zh'?'发布动态':'Post Update' }}</h2><button @click="showPostModal=false">×</button></header>
        <label>{{ lang==='zh'?'动态分类':'Category' }}<select v-model="postForm.category"><option value="share">{{ lang==='zh'?'分享':'Share' }}</option><option value="help">{{ lang==='zh'?'求助':'Help' }}</option><option value="team">{{ lang==='zh'?'组队':'Team up' }}</option><option value="interaction">{{ lang==='zh'?'互动':'Interaction' }}</option></select></label>
        <label>{{ lang==='zh'?'内容':'Content' }}<textarea v-model="postForm.content" :placeholder="lang==='zh'?'分享你的健身动态...':'Share your fitness update...'"></textarea></label>
        <footer><button class="ghost" @click="showPostModal=false">{{ lang==='zh'?'取消':'Cancel' }}</button><button class="primary" @click="submitPostForm">{{ lang==='zh'?'发布动态':'Post Update' }}</button></footer>
      </section>
    </div>

    <div v-if="replyTarget" class="clean-modal-backdrop" @click.self="replyTarget=null">
      <section class="clean-social-modal reply-box">
        <header><h2>💬 {{ lang==='zh'?'回复动态':'Reply' }}</h2><button @click="replyTarget=null">×</button></header>
        <article class="reply-preview"><b>{{ replyTarget.author || replyTarget.username }}</b><p>{{ replyTarget.content }}</p></article>
        <label>{{ lang==='zh'?'回复内容':'Reply content' }}<textarea v-model="replyText" :placeholder="lang==='zh'?'写下你的回复...':'Write your reply...'"></textarea></label>
        <footer><button class="ghost" @click="replyTarget=null">{{ lang==='zh'?'取消':'Cancel' }}</button><button class="primary" @click="submitReply">{{ lang==='zh'?'发送':'Send' }}</button></footer>
      </section>
    </div>

  </div>
</template>

<script>
import { defineComponent, h } from 'vue'

const dictionary = {
  zh: { username:'用户名', password:'密码', login:'登录', register:'注册', dashboard:'首页', classes:'课程预约', profile:'个人中心', mall:'积分商城', community:'社区', checkin:'打卡签到', ranking:'排行榜', logout:'退出登录', points:'积分', users:'用户管理', products:'商品管理', adminDashboard:'会员管理', coachDashboard:'教练首页', mailbox:'邮箱', coachProfile:'我的资料' },
  en: { username:'Username', password:'Password', login:'Login', register:'Register', dashboard:'Dashboard', classes:'Class Booking', profile:'My Profile', mall:'Points Mall', community:'Community', checkin:'Check-in', ranking:'Ranking', logout:'Logout', points:'Points', users:'User Management', products:'Product Management', adminDashboard:'Membership', coachDashboard:'Coach Dashboard', mailbox:'Mailbox', coachProfile:'My Profile' }
}

const AdminTable = defineComponent({
  props: ['rows','lang','mode'], emits:['edit','remove'],
  methods: {
    labelKey(row){ const s=String(row.levelKey||row.membershipType||''); return s.toLowerCase().includes('vip')?'vip':'normal' },
    label(v){ const s=String(v||''); return s.toLowerCase().includes('vip') ? 'VIP Member' : 'Normal Member' },
    roleLabel(v){ return String(v||'USER').toUpperCase()==='ADMIN'?'Administrator':'Member' }
  },
  render(){
    const rows = Array.isArray(this.rows) ? this.rows : []
    const usersMode = this.mode === 'users'
    const heads = ['ID','Username','Name']
    if (usersMode) heads.push('Phone')
    heads.push('Email')
    heads.push(usersMode ? 'Role' : 'Member Level')
    heads.push('Status','Actions')
    const header = h('thead', [h('tr', heads.map(x => h('th', x)))])
    const body = h('tbody', rows.map(row => {
      const cells = [h('td', row.id), h('td', row.username || ''), h('td', row.name || '')]
      if (usersMode) cells.push(h('td', row.phone || ''))
      cells.push(h('td', row.email || ''))
      if (usersMode) cells.push(h('td', [h('span', {class:'ft-badge'}, this.roleLabel(row.role))]))
      else cells.push(h('td', [h('span', {class:['ft-badge', this.labelKey(row)]}, this.label(row.levelKey || row.membershipType))]))
      const inactive = row.statusKey === 'inactive' || row.active === false
      cells.push(h('td', [h('span', {class:['ft-status', inactive ? 'off' : 'on']}, inactive ? 'Inactive' : 'Active')]))
      cells.push(h('td', [
        h('button', {onClick:()=>this.$emit('edit', row)}, 'Edit'),
        h('button', {onClick:()=>this.$emit('remove', row)}, 'Delete')
      ]))
      return h('tr', {key: row.id}, cells)
    }))
    return h('div', {class:'ft-table'}, [h('table', [header, body])])
  }
})

export default defineComponent({
  components: { AdminTable },
  data(){
    return {
      route: window.location.pathname === '/' ? '/user/login' : window.location.pathname,
      lang: localStorage.getItem('fitlife_lang_v8') || 'zh',
      token: localStorage.getItem('fitlife_token') || '',
      sessionName: localStorage.getItem('fitlife_name') || '用户',
      userName: localStorage.getItem('fitlife_username') || localStorage.getItem('fitlife_name') || 'fitness_pro',
      message: '',
      loginForm: { username:'', password:'', name:'', email:'', phone:'' },
      authLoading: false,
      points: 0, isVip: false, checkedToday: false, membershipEndDate: '', redeemingId: null, bookingBusyIds: [], apiInFlight: {},
      profile: {}, classes: [], privateClasses: [], coaches: [], goods: [], products: [], teams: [], ranking: [], calendar: [], bookings: [], bookedIds: [], exchanges: [],
      dash: {}, adminUsers: [], adminReports: [], adminStats: {totalMembers:0,activeMembers:0,newMembers:0},
      coachClasses: [], coachStats:{activeUsers:0}, notifications: [], complaints: [], reviews: [], coachProfile: {}, coachSchedule: [], coachFacilities: [], coachFacilityBookings: [], coachStudents: [], coachSubTab:'mine', coachClassFilter:'all', coachStudentSearch:'', coachEditDialog:null, coachMailDialog:null, coachMailReply:'', coachPendingFacility:null, coachClassForm:{name:'',date:'',startTime:'',endTime:'',reservedBookingId:'',location:'',capacity:20,description:''}, coachNotify:{classReminders:true,bookingAlerts:true,reviewAlerts:true,complaintAlerts:true},
      newPost:'', selectedPlan:'year', classTab:'group', activeDay:'today', mallFilter:'all', selectedCoach:null, selectedCoachCourses:[], profileTab:'basic', userComplaints:[], complaintType:'course', complaintContent:'',
      socialSort:'hot', socialFilter:'all', rankingPeriod:'total', likedPosts:[], showTeamModal:false, showPostModal:false, replyTarget:null,
      teamForm:{title:'',description:'',category:'running',location:'',meetTime:'',maxMembers:20},
      postForm:{content:'',category:'share'}, replyText:'', selectedPayment:'wechat', showPayModal:false, adminDialog:null, adminForm:{}, adminClassTab:'group', adminProductSearch:'', adminProductCategory:'all', adminCommunityTab:'posts'
    }
  },
  computed: {
    authPage(){ return ['/user/login','/user/register','/admin/login','/coach/login'].includes(this.route) },
    area(){ if(this.route.startsWith('/admin')) return 'admin'; if(this.route.startsWith('/coach')) return 'coach'; return 'user' },
    authTitle(){ if(this.route==='/admin/login') return this.lang==='zh'?'管理员登录':'Admin Login'; if(this.route==='/coach/login') return this.lang==='zh'?'教练登录':'Coach Login'; if(this.route==='/user/register') return this.lang==='zh'?'创建账号':'Create Account'; return this.lang==='zh'?'用户登录':'User Login' },
    authThemeClass(){ if(this.route.includes('admin')) return 'ft-admin-auth'; if(this.route.includes('coach')) return 'ft-coach-auth'; return 'ft-user-auth' },
    authHeroTitle(){ if(this.route.includes('admin')) return this.lang==='zh'?'高效管理健身房业务':'Manage FitLife Operations'; if(this.route.includes('coach')) return this.lang==='zh'?'健身用户服务平台':'Fitness User Service Platform'; return this.lang==='zh'?'开启你的健身旅程':'Start Your Fitness Journey' },
    authSubtitle(){ if(this.route.includes('admin')) return this.lang==='zh'?'健身房后台管理系统':'Gym Management System'; if(this.route.includes('coach')) return this.lang==='zh'?'课程预约、打卡积分与社区互动一站式服务':'Book classes, check in, earn points and join the community'; return this.lang==='zh'?'欢迎来到 FitLife 健身服务平台':'Welcome to FitLife Fitness Service Platform' },
    authButton(){ return this.route==='/user/register' ? this.t('register') : this.t('login') },
    demoUserAccounts(){ return [
      {key:'user',username:'fitness_pro',icon:'👤',zh:'会员端 · 健身狂人',en:'User Portal · Fitness Pro'},
      {key:'user2',username:'strength_member',icon:'🏋️',zh:'会员端 · 力量小子',en:'User Portal · Strength Member'},
      {key:'user3',username:'yoga_member',icon:'🧘',zh:'会员端 · 瑜伽达人',en:'User Portal · Yoga Member'}
    ] },
    alternateAuthEntries(){
      const user={path:'/user/login',icon:'👤',title:this.lang==='zh'?'用户入口':'User Login',sub:'FitLife User'}
      const admin={path:'/admin/login',icon:'👔',title:this.lang==='zh'?'管理员入口':'Admin Login',sub:'FitLife Admin'}
      const coach={path:'/coach/login',icon:'👨‍🏫',title:this.lang==='zh'?'教练入口':'Coach Login',sub:'FitLife Coach'}
      if(this.route.includes('admin')) return [user,coach]
      if(this.route.includes('coach')) return [user,admin]
      return [admin,coach]
    },
    paymentTitle(){ const names={wechat:'WeChat Pay',alipay:'Alipay',card:this.lang==='zh'?'银行卡支付':'Bank Card'}; return names[this.selectedPayment] || 'WeChat Pay' },
    userMenu(){ return [ {path:'/user/dashboard',key:'dashboard'}, {path:'/user/classes',key:'classes'}, {path:'/user/profile',key:'profile'}, {path:'/user/points-mall',key:'mall'}, {path:'/user/social',key:'community'}, {path:'/user/checkin',key:'checkin'}, {path:'/user/ranking',key:'ranking'} ] },
    adminMenu(){ return [ {path:'/admin/dashboard',key:'adminDashboard',icon:'👥'}, {path:'/admin/users',key:'users',icon:'⚙️'}, {path:'/admin/classes',key:'classes',icon:'🗓️'}, {path:'/admin/products',key:'products',icon:'🛍️'}, {path:'/admin/community',key:'community',icon:'💬'} ] },
    coachMenu(){ return [ {path:'/coach/dashboard',key:'coachDashboard',icon:'📊'}, {path:'/coach/profile',key:'coachProfile',icon:'👤'}, {path:'/coach/classes',key:'classes',icon:'📅'}, {path:'/coach/mailbox',key:'mailbox',icon:'✉️'} ] },
    adminTitle(){ const m=this.adminMenu.find(x=>x.path===this.route); return m ? this.t(m.key) : this.t('adminDashboard') },
    adminDialogTitle(){ if(!this.adminDialog) return ''; const mode=this.adminDialog.mode==='add'?'Add':'Edit'; if(this.adminDialog.kind==='class') return `${mode} Class`; if(this.adminDialog.kind==='product') return `${mode} Product`; if(this.adminDialog.kind==='post') return 'Edit post'; return `${mode} ${this.adminDialog.kind==='member'?'Member':'User'}` },
    adminClassRows(){ return this.classes.filter(c=>this.adminClassTab==='private'?this.isPrivateAdminClass(c):!this.isPrivateAdminClass(c)) },
    productCategoryOptions(){ return [
      {value:'Coupons',zh:'优惠券',en:'Coupons'},
      {value:'Equipment',zh:'运动装备',en:'Equipment'},
      {value:'Training Accessories',zh:'训练配件',en:'Training Accessories'},
      {value:'Supplements',zh:'营养补剂',en:'Supplements'},
      {value:'Apparel',zh:'运动服饰',en:'Apparel'},
      {value:'Sports Accessories',zh:'运动配件',en:'Sports Accessories'},
      {value:'VIP Exclusive',zh:'VIP专属',en:'VIP Exclusive'},
      {value:'Member Services',zh:'会员服务',en:'Member Services'},
      {value:'Member Privileges',zh:'会员权益',en:'Member Privileges'},
      {value:'Other',zh:'其他',en:'Other'}
    ] },
    adminProductCategories(){
      const base=this.productCategoryOptions.map(x=>x.value)
      const extra=(this.goods||[]).map(p=>p.category).filter(Boolean).filter(x=>!base.includes(x))
      return [...base, ...new Set(extra)]
    },
    filteredAdminProducts(){ const q=String(this.adminProductSearch||'').toLowerCase(); return (this.goods||[]).filter(p=>{ const text=[p.name,p.nameEn,p.description,p.descriptionEn,p.category].join(' ').toLowerCase(); const catOk=this.adminProductCategory==='all' || p.category===this.adminProductCategory; return catOk && (!q || text.includes(q)) }) },
    coachTitle(){ const m=this.coachMenu.find(x=>x.path===this.route); return m ? this.t(m.key) : this.t('coachDashboard') },
    coachNotifyRows(){ return [
      {key:'classReminders',title:'Class reminders',desc:'Remind you before classes start'},
      {key:'bookingAlerts',title:'Booking alerts',desc:'When members book or cancel'},
      {key:'reviewAlerts',title:'Review alerts',desc:'When you receive a new review'},
      {key:'complaintAlerts',title:'Complaint alerts',desc:'When you receive a complaint'}
    ] },
    coachTodaySchedule(){ return this.coachSchedule.length ? this.coachSchedule : (this.coachClasses||[]).filter(c=>this.itemDateKey(c)===this.dateKeyLocal(new Date())) },
    coachPendingCount(){ return (this.complaints||[]).filter(c=>String(c.status||'').toLowerCase().includes('pending')).length + (this.reviews||[]).filter(r=>!r.responded).length },
    coachMonthlyCount(){ const m=new Date().getMonth(); return (this.coachClasses||[]).filter(c=>{ const d=new Date(c.startTime||c.date||0); return !Number.isNaN(d.getTime()) && d.getMonth()===m }).length },
    filteredCoachClasses(){ const all=this.coachClasses||[]; if(this.coachClassFilter==='all') return all; return all.filter(c=>this.coachClassStatusKey(c)===this.coachClassFilter) },
    availableCoachBookings(){ return (this.coachFacilityBookings||[]).filter(b=>!b.usedByClass && String(b.status||'RESERVED').toUpperCase()!=='CANCELLED') },
    visibleCoachStudents(){ const q=String(this.coachStudentSearch||'').toLowerCase(); return (this.coachStudents||[]).filter(s=>!q || [s.name,s.username,s.phone].join(' ').toLowerCase().includes(q)) },
    shortClasses(){ return this.classes.slice(0, 3) },
    firstClass(){ return this.classes[0] || {} },
    groupClassCount(){ return this.classes.length || 5 },
    bookedClassCount(){ return this.bookings.length },
    coachCount(){ return this.coaches.length || 4 },
    privateCoaches(){ return (this.coaches.length ? this.coaches : [
      {id:1,name:'张教练',avatar:'👨‍🏫',specialty:'力量训练,增肌,功能性训练',rating:'4.8',experienceYears:5,hourlyRate:300},
      {id:2,name:'李教练',avatar:'🧘',specialty:'瑜伽,普拉提,拉伸',rating:'4.9',experienceYears:6,hourlyRate:350},
      {id:3,name:'王教练',avatar:'🚴',specialty:'减脂,康复,体态矫正',rating:'5',experienceYears:7,hourlyRate:400},
      {id:4,name:'赵教练',avatar:'🥊',specialty:'散打搏击,爆发力,功能性训练',rating:'5',experienceYears:8,hourlyRate:450}
    ]).slice(0,4) },
    bookingDays(){ const base=new Date(); const names=this.lang==='zh'?['今天','明天','周一','周二','周三','周四','周五']:['Today','Tomorrow','Mon','Tue','Wed','Thu','Fri']; return Array.from({length:7},(_,i)=>{const d=new Date(base); d.setDate(base.getDate()+i); return {key:i===0?'today':'d'+i,label:names[i],date:(d.getMonth()+1)+'/'+d.getDate()}}) },
    groupDisplayClasses(){ const all=(this.classes||[]).filter(c=>String(c.type||'GROUP').toUpperCase()!=='PRIVATE'); const day=this.activeDateKey(); let rows=all.filter(c=>this.itemDateKey(c)===day); if(!rows.length && this.activeOffset()===0) rows=all.slice(0,3); return rows.slice(0,8) },
    filteredBookings(){ const day=this.activeDateKey(); return (this.bookings||[]).filter(b=>this.itemDateKey(b.classes||b)===day) },
    mallTabs(){ return this.lang==='zh' ? [{key:'all',icon:'',label:'全部商品'},{key:'coupon',icon:'🎟️',label:'优惠券'},{key:'gear',icon:'🏋️',label:'运动装备'},{key:'vip',icon:'👑',label:'VIP专属'}] : [{key:'all',icon:'',label:'All Items'},{key:'coupon',icon:'🎟️',label:'Coupons'},{key:'gear',icon:'🏋️',label:'Gear'},{key:'vip',icon:'👑',label:'VIP Only'}] },
    shownGoods(){ return (this.goods||[]).filter(p=>{ if(this.mallFilter==='all') return true; const cat=String(p.category||p.discountType||p.name||'').toLowerCase(); if(this.mallFilter==='coupon') return cat.includes('券')||cat.includes('coupon'); if(this.mallFilter==='gear') return cat.includes('装备')||cat.includes('product')||cat.includes('运动'); if(this.mallFilter==='vip') return this.isVipGood(p); return true }) },
    todayShort(){ const d=new Date(); return d.toLocaleDateString(this.lang==='zh'?'zh-CN':'en-US',{weekday:'short',month:'short',day:'numeric'}) },
    goalProgress(){ return Math.min(100, Math.max(14, Math.round(((this.dash.streak || 1)%7)/7*100) || 14)) },
    recentPosts(){ const rows=[...(this.posts||[])].sort((a,b)=>Number(b.pinned||0)-Number(a.pinned||0) || Number(b.likes||0)-Number(a.likes||0)).slice(0,2); if(rows.length) return rows; return this.lang==='zh' ? [{id:'local-1',author:'健身狂人',username:'fitness_pro',content:'今天完成了10公里跑步，感觉太棒了！坚持就是胜利！',likes:128,comments:15,createdAt:new Date().toISOString()},{id:'local-2',author:'瑜伽达人',username:'yoga_member',content:'分享一个超轻松的瑜伽拉伸视频，大家可以试试！',likes:89,comments:8,createdAt:new Date().toISOString()}] : [{id:'local-1',author:'Fitness Pro',username:'fitness_pro',content:'Finished a 10K run today — feels amazing! Keep going!',likes:128,comments:15,createdAt:new Date().toISOString()},{id:'local-2',author:'Yoga Member',username:'yoga_member',content:'Sharing a simple yoga stretch routine — give it a try!',likes:89,comments:8,createdAt:new Date().toISOString()}] },
    rankPeriodTabs(){ return this.lang==='zh' ? [{key:'total',icon:'📊',label:'总排行榜'},{key:'week',icon:'🗓️',label:'本周排行'},{key:'month',icon:'🗓️',label:'本月排行'}] : [{key:'total',icon:'📊',label:'Overall'},{key:'week',icon:'🗓️',label:'Weekly'},{key:'month',icon:'🗓️',label:'Monthly'}] },
    rankList(){ return this.ranking.length ? this.ranking : [] },
    socialSortText(){ return this.lang==='zh' ? (this.socialSort==='hot'?'排序：按热度':'排序：按最新') : (this.socialSort==='hot'?'Sort: Hot':'Sort: Latest') },
    socialFilterLabel(){ const zh={all:'全部',share:'分享',help:'求助',team:'组队',interaction:'互动'}; const en={all:'All',share:'Share',help:'Help',team:'Team up',interaction:'Interaction'}; return (this.lang==='zh'?zh:en)[this.socialFilter] || this.socialFilter },
    filteredPosts(){
      const mapCat=(v)=>{ const x=String(v||'').toLowerCase(); if(x.includes('help')||x.includes('求助')) return 'help'; if(x.includes('team')||x.includes('组队')) return 'team'; if(x.includes('互动')||x.includes('interaction')) return 'interaction'; if(x.includes('checkin')||x.includes('打卡')) return 'interaction'; return 'share' }
      let arr=[...(this.posts||[])].filter(p=>this.socialFilter==='all' || mapCat(p.category)===this.socialFilter)
      if(this.socialSort==='latest') arr.sort((a,b)=>new Date(b.createdAt||0)-new Date(a.createdAt||0))
      else arr.sort((a,b)=>((b.likes||0)*2+(b.comments||0))-((a.likes||0)*2+(a.comments||0)))
      arr.sort((a,b)=>(b.pinned===true)-(a.pinned===true))
      return arr
    },
    topRanks(){ return this.rankList.slice(0,3) },
    userHandle(){ return this.userName || this.sessionName || 'fitness_pro' },
    vipPlans(){ return this.lang==='zh' ? [
      {id:'month',name:'月卡会员',price:99,desc:'适合短期体验',features:['会员课程折扣','积分加速','专属活动'],hot:false},
      {id:'year',name:'VIP年卡',price:699,desc:'最受欢迎的长期训练方案',features:['全年会员权益','私教优先预约','积分商城专属商品','社区身份标识'],hot:true},
      {id:'family',name:'家庭卡',price:999,desc:'适合多人共同使用',features:['最多3位成员','课程共享权益','家庭健康报告'],hot:false}
    ] : [
      {id:'month',name:'Monthly Plan',price:99,desc:'Best for short trials',features:['Member class discount','Faster points','Exclusive events'],hot:false},
      {id:'year',name:'VIP Annual Plan',price:699,desc:'Most popular long-term plan',features:['Full-year benefits','Priority personal training','VIP mall products','Community identity badge'],hot:true},
      {id:'family',name:'Family Plan',price:999,desc:'For multiple members',features:['Up to 3 members','Shared class benefits','Family health report'],hot:false}
    ]},
    badges(){ return this.lang==='zh' ? [{icon:'🔥',name:'连续打卡',desc:'坚持7天打卡'},{icon:'🏅',name:'训练达人',desc:'完成30节课程'},{icon:'💎',name:'积分明星',desc:'累计获得1000积分'}] : [{icon:'🔥',name:'Streak',desc:'Check in for 7 days'},{icon:'🏅',name:'Training Pro',desc:'Finish 30 classes'},{icon:'💎',name:'Points Star',desc:'Earn 1000 points'}] },
    socialAds(){ return this.lang==='zh' ? [{title:'新会员优惠',description:'新会员开卡享受课程礼包和积分奖励'},{title:'私教课特惠',description:'购买私教课程包立享额外训练评估'}] : [{title:'New member offer',description:'New members get class packs and points rewards'},{title:'Private class deal',description:'Buy private training packs with extra assessment'}] },
    sortedTeams(){ const rank=t=> t.createdByCurrentUser ? 0 : (t.joined ? 1 : 2); return [...(this.teams||[])].sort((a,b)=>rank(a)-rank(b) || Number(a.id||0)-Number(b.id||0)) },
    checkinDateTitle(){ const d=new Date(); return this.lang==='zh' ? `${d.getFullYear()}年${d.getMonth()+1}月${d.getDate()}日` : d.toLocaleDateString('en-US',{year:'numeric',month:'long',day:'numeric'}) },
    checkinWeekday(){ return new Date().toLocaleDateString(this.lang==='zh'?'zh-CN':'en-US',{weekday:'long'}) },
    badgeV19(){ const streak=Number(this.dash.streak||1); const zh=[['🥉','初露锋芒','完成首次打卡',streak>=1],['🥈','坚持不懈','连续打卡7天',streak>=7],['🥇','持之以恒','连续打卡30天',streak>=30],['💎','钻石会员','连续打卡100天',streak>=100]]; const en=[['🥉','First Step','Complete first check-in',streak>=1],['🥈','Persistent','Check in for 7 days',streak>=7],['🥇','Consistent','Check in for 30 days',streak>=30],['💎','Diamond','Check in for 100 days',streak>=100]]; return (this.lang==='zh'?zh:en).map(x=>({icon:x[0],name:x[1],desc:x[2],unlocked:x[3]})) },
    rankPodium(){ return {first:this.rankList[0],second:this.rankList[1],third:this.rankList[2]} }
  },
  mounted(){ window.addEventListener('popstate',()=>{this.route=window.location.pathname; if(this.authPage) this.applyLoginDefaults(); this.load()}); if(this.authPage) this.applyLoginDefaults(); this.load() },
  watch: {
    route(){ if(this.authPage) this.applyLoginDefaults() }
  },
  methods: {
    t(key){ return dictionary[this.lang][key] || key },
    setLang(v){ this.lang=v; localStorage.setItem('fitlife_lang_v8',v); document.documentElement.lang=v==='zh'?'zh-CN':'en' },
    demoCreds(kind){ const map={user:{username:'fitness_pro',password:'123456'},user2:{username:'strength_member',password:'123456'},user3:{username:'yoga_member',password:'123456'},admin:{username:'admin',password:'admin123'},coach:{username:'coach_1',password:'123456'}}; return map[kind] || map.user },
    currentLoginKind(){ return this.route.includes('admin') ? 'admin' : (this.route.includes('coach') ? 'coach' : 'user') },
    applyLoginDefaults(){ this.authLoading=false; const c=this.demoCreds(this.currentLoginKind()); this.loginForm={...this.loginForm, username:c.username, password:c.password} },
    fillDemo(kind){ this.authLoading=false; const path=kind==='admin'?'/admin/login':kind==='coach'?'/coach/login':'/user/login'; if(this.route!==path) this.go(path); const c=this.demoCreds(kind); this.loginForm={...this.loginForm, username:c.username, password:c.password} },
    go(path){
      // Any navigation away from a login page must release the login button.
      this.authLoading=false
      if(this._activeAbort){ try{ this._activeAbort.abort() }catch(e){}; this._activeAbort=null }
      history.pushState({},'',path)
      this.route=path
      this.message=''
      if(['/user/login','/user/register','/admin/login','/coach/login'].includes(path)){
        this.applyLoginDefaults()
        return
      }
      this.load().catch(err=>{ console.error(err); this.message=this.lang==='zh'?'数据加载失败，请刷新重试':'Data loading failed, please refresh' })
    },
    logout(){
      const oldArea=this.area
      const target=oldArea==='admin'?'/admin/login':oldArea==='coach'?'/coach/login':'/user/login'
      if(this._activeAbort){ try{ this._activeAbort.abort() }catch(e){}; this._activeAbort=null }
      this.authLoading=false
      localStorage.removeItem('fitlife_token')
      localStorage.removeItem('fitlife_name')
      localStorage.removeItem('fitlife_username')
      this.token=''
      this.sessionName=''
      this.userName=''
      this.message=''
      history.replaceState({},'',target)
      this.route=target
      this.applyLoginDefaults()
    },
    apiBase(){ return '' },
    directApiBase(){
      const port=window.location.port
      const host=window.location.hostname || '127.0.0.1'
      if(port && port !== '8080') return `${window.location.protocol}//${host}:8080`
      return ''
    },
    async api(path, opts={}){
      const method=String(opts.method||'GET').toUpperCase()
      // Do not reuse in-flight responses for highly mutable data. A booking mutation can
      // finish while an older /api/bookings/my or /api/classes request is still resolving;
      // reusing that stale response makes the button flip to "Book" and then back to
      // "Cancel Booking". Keep de-duplication only for relatively static GET calls.
      const mutableGet = path.includes('/api/bookings') || path.includes('/api/classes') || path.includes('/api/trainers')
      const inFlightKey=(method==='GET' && !opts.noDedup && !mutableGet) ? `${method}:${path}:${this.token||''}` : ''
      if(inFlightKey && this.apiInFlight[inFlightKey]) return this.apiInFlight[inFlightKey]
      const run=(async()=>{
        const timeout=Number(opts.timeout || (path.includes('/login') ? 7000 : 12000))
        const isFormData=typeof FormData!=='undefined' && opts.body instanceof FormData
        const {timeout:ignoredTimeout, headers:customHeaders, noDedup:ignoredNoDedup, ...fetchOptions}=opts
        const headers={...(isFormData?{}:{'Content-Type':'application/json'}),...(this.token?{Authorization:'Bearer '+this.token}:{}),...(customHeaders||{})}
        const requestOnce=async (base)=>{
          const controller=new AbortController()
          const timer=setTimeout(()=>controller.abort(), timeout)
          try{
            const res=await fetch(base+path,{...fetchOptions,signal:controller.signal,headers})
            const text=await res.text()
            if(!text) return {success:res.ok}
            let data
            try{ data=JSON.parse(text) }catch(e){ data={success:false,message:text} }
            if(!res.ok && data.success!==false) data.success=false
            return data
          } finally { clearTimeout(timer) }
        }
        try{
          const first=await requestOnce(this.apiBase())
          if(first && first.success!==false) return first
          const direct=this.directApiBase()
          if(direct){
            const retry=await requestOnce(direct)
            if(retry && retry.success!==false) return retry
            return retry && retry.message ? retry : first
          }
          return first
        }catch(e){
          try{
            const direct=this.directApiBase()
            if(direct) return await requestOnce(direct)
          }catch(e2){
            const msg=e2 && e2.name==='AbortError' ? (this.lang==='zh'?'请求超时，请确认 Spring Boot 8080 已启动、MySQL 密码为 lnj031212':'Request timed out. Please check Spring Boot 8080 and MySQL password lnj031212') : (this.lang==='zh'?'无法连接后端服务，请确认 8080 已启动':'Cannot connect to backend service. Please confirm port 8080 is running')
            return {success:false,message:msg,error:String(e2)}
          }
          const msg=e && e.name==='AbortError' ? (this.lang==='zh'?'请求超时，请确认 Spring Boot 8080 已启动、MySQL 密码为 lnj031212':'Request timed out. Please check Spring Boot 8080 and MySQL password lnj031212') : (this.lang==='zh'?'无法连接后端服务，请确认 8080 已启动':'Cannot connect to backend service. Please confirm port 8080 is running')
          return {success:false,message:msg,error:String(e)}
        }
      })()
      if(inFlightKey){
        this.apiInFlight[inFlightKey]=run
        run.finally(()=>{ if(this.apiInFlight[inFlightKey]===run) delete this.apiInFlight[inFlightKey] })
      }
      return run
    },
    async submitAuth(){
      if(this.authLoading) return
      this.message=''
      this.authLoading=true
      try{
        const kind=this.currentLoginKind()
        const defaults=this.demoCreds(kind)
        let url='/api/auth/login'
        let body={username:(this.loginForm.username || defaults.username).trim(), password:this.loginForm.password || defaults.password}
        if(this.route==='/user/register'){ url='/api/auth/register'; body={...this.loginForm} }
        if(this.route==='/admin/login'){ url='/api/admin/auth/login'; body={username:(this.loginForm.username || defaults.username).trim(), password:this.loginForm.password || defaults.password} }
        if(this.route==='/coach/login'){ url='/api/coach/login'; body={username:(this.loginForm.username || defaults.username).trim(), password:this.loginForm.password || defaults.password} }
        const out=await this.api(url,{method:'POST',body:JSON.stringify(body),timeout:7000})
        if(!out.success){ this.message=out.message || (this.lang==='zh'?'登录失败':'Login failed'); return }
        const token=out.token || out.data?.token || out.data?.accessToken || ''
        const name=out.username || out.data?.username || body.username
        this.token=token
        this.sessionName=name
        this.userName=body.username
        localStorage.setItem('fitlife_token',token)
        localStorage.setItem('fitlife_name',name)
        localStorage.setItem('fitlife_username',body.username)
        const target=this.route.includes('admin')?'/admin/dashboard':(this.route.includes('coach')?'/coach/dashboard':'/user/dashboard')
        history.replaceState({},'',target)
        this.route=target
        // Do not keep the login button in loading state while dashboard data loads.
        this.authLoading=false
        this.load().catch(err=>{ console.error(err); this.message=this.lang==='zh'?'登录成功，但数据加载失败，请刷新':'Signed in, but data loading failed. Please refresh' })
        return
      }catch(e){
        console.error(e)
        this.message=this.lang==='zh'?'无法连接后端服务':'Cannot connect to backend service'
      }finally{
        this.authLoading=false
      }
    },
    async load(){
      if(this.authPage) return
      if(this.area==='user') await this.loadUser()
      if(this.area==='admin') await this.loadAdmin()
      if(this.area==='coach') await this.loadCoach()
    },
    async loadUser(){ await Promise.allSettled([this.loadPoints(),this.loadProfile(),this.loadClasses(),this.loadTrainers(),this.loadGoods(),this.loadSocial(),this.loadRanking(),this.loadCheckin(),this.loadUserComplaints()]) },
    async loadPoints(){
      const o=await this.api('/api/points/my')
      if(o.success===false) return
      const d=o.data||{}
      const nextPoints=Number(d.availablePoints ?? d.totalPoints ?? this.points)
      if(Number.isFinite(nextPoints)) this.points=nextPoints
      try{
        const s=await this.api('/api/checkin/status')
        if(s.success===false) return
        const sd=s.data||{}
        const streak=Number(sd.consecutiveDays ?? sd.streakDays ?? this.dash.streak ?? 0)
        this.dash.streak=streak
        this.dash.badges=[1,7,30,100].filter(x=>streak>=x).length || (streak>0?1:0)
      }catch(e){ /* keep current point/check-in display */ }
    },
    async loadProfile(){
      const o=await this.api('/api/users/profile')
      if(o.success!==false && o.data){ this.profile=o.data||{}; this.sessionName=this.profile.name || this.sessionName }
      const m=await this.api('/api/user/membership')
      if(m.success!==false && m.data){ const md=m.data||{}; const mt=String(md.membershipType||''); this.isVip=mt.toLowerCase().includes('vip'); this.membershipEndDate=md.endDate||this.membershipEndDate }
    },
    async loadClasses(){
      const [o,b]=await Promise.allSettled([this.api('/api/classes',{noDedup:true}),this.api('/api/bookings/my',{noDedup:true})])
      const bookingRows=Array.isArray(b.value?.data)?b.value.data:null
      const classRows=Array.isArray(o.value?.data)?o.value.data:null
      if(bookingRows) this.syncBookings(bookingRows)
      else if(classRows){
        // Fallback to backend class flags when /bookings/my is temporarily unavailable.
        this.bookedIds=[...new Set(classRows.filter(c=>c.isBooked).map(c=>this.classIdOf(c)).filter(x=>x!==null))]
      }
      if(classRows && classRows.length){
        this.classes=classRows.map(c=>this.withBookingFlag(c))
      }else if(!this.classes.length){
        this.classes=this.defaultClassRows().map(c=>this.withBookingFlag(c))
      }
    },
    async loadTrainers(){ const o=await this.api('/api/trainers'); if(Array.isArray(o.data)&&o.data.length) this.coaches=o.data },
    async loadGoods(){
      const o=await this.api('/api/points/products')
      if(Array.isArray(o.data) && o.data.length){ this.goods=o.data }
      else if(!this.goods.length){ this.goods=this.defaultPointGoods() }
      const p=await this.api('/api/products')
      if(Array.isArray(p.data) && p.data.length) this.products=p.data
      try{
        const h=await this.api('/api/points/history')
        if(Array.isArray(h.data)) this.exchanges=h.data
      }catch(e){ /* keep current redemption records instead of clearing the mall UI */ }
    },
    async loadSocial(){
      const p=await this.api('/api/posts')
      if(Array.isArray(p.data)){
        const rawPosts=p.data
        this.posts=await Promise.all(rawPosts.map(async (post,index)=>{
          if(index>=12) return {...post,replies:Array.isArray(post.replies)?post.replies.slice(0,3):[]}
          try{ const c=await this.api(`/api/posts/${post.id}/comments`); return {...post,replies:Array.isArray(c.data)?c.data.slice(0,3):[]} }catch(e){ return {...post,replies:Array.isArray(post.replies)?post.replies.slice(0,3):[]} }
        }))
      }else if(!this.posts.length){ this.posts=[] }
      const t=await this.api('/api/teams')
      if(Array.isArray(t.data)) this.teams=t.data
    },
    async loadRanking(){ const o=await this.api(`/api/ranking/${this.rankingPeriod}`); if(Array.isArray(o.data)) this.ranking=o.data },
    async loadCheckin(){
      const now=new Date()
      const o=await this.api(`/api/checkin/records/month?year=${now.getFullYear()}&month=${now.getMonth()+1}`)
      if(Array.isArray(o.data) || !this.calendar.length){
        const done=new Set((Array.isArray(o.data)?o.data:[]).map(x=>Number(String(x.checkinDate||x.date).slice(-2))))
        const days=new Date(now.getFullYear(),now.getMonth()+1,0).getDate()
        this.calendar=Array.from({length:days},(_,i)=>({n:i+1,done:done.has(i+1),today:i+1===now.getDate(),future:i+1>now.getDate()}))
        this.checkedToday=done.has(now.getDate())
      }
      try{ const status=await this.api('/api/checkin/status'); const st=status.data||{}; this.dash.streak=st.consecutiveDays||st.streakDays||this.dash.streak||0; this.dash.badges=[1,7,30,100].filter(x=>Number(this.dash.streak)>=x).length || (this.dash.streak>0?1:0) }catch(e){}
    },
    async loadUserComplaints(){ try{ const o=await this.api('/api/complaints/user'); this.userComplaints=Array.isArray(o.data)?o.data:[] }catch(e){ this.userComplaints=[] } },
    async loadAdmin(){
      const [u,r,c,p,po]=await Promise.allSettled([
        this.api('/api/admin/users'),
        this.api('/api/admin/reports'),
        this.api('/api/admin/classes'),
        this.api('/api/admin/products'),
        this.api('/api/admin/posts')
      ])
      const uv=u.value||{}
      this.adminUsers=Array.isArray(uv.data)?uv.data:[]
      this.adminStats=uv.stats||{totalMembers:this.adminUsers.length,activeMembers:this.adminUsers.filter(x=>x.active!==false).length,newMembers:3}
      this.adminReports=Array.isArray(r.value?.data)?r.value.data:[]
      this.classes=Array.isArray(c.value?.data)?c.value.data.map(x=>this.withBookingFlag(x)):[]
      this.goods=Array.isArray(p.value?.data)?p.value.data:[]
      this.posts=Array.isArray(po.value?.data)?po.value.data:[]
      await Promise.allSettled([this.loadTrainers()])
    },
    async loadCoach(){
      const [a,c,n,cm,r,p,s,f,fb,ts]=await Promise.allSettled([
        this.api('/api/coach/active-users'),this.api('/api/coach/classes'),this.api('/api/coach/notifications'),this.api('/api/coach/complaints'),this.api('/api/coach/reviews'),this.api('/api/coach/profile'),this.api('/api/coach/students'),this.api('/api/coach/facilities'),this.api('/api/coach/facilities/my-bookings'),this.api('/api/coach/today-schedule')
      ])
      if(a.value?.success!==false && a.value?.data!==undefined) this.coachStats.activeUsers=a.value.data
      if(Array.isArray(c.value?.data)) this.coachClasses=c.value.data
      if(Array.isArray(n.value?.data)) this.notifications=n.value.data
      if(Array.isArray(cm.value?.data)) this.complaints=cm.value.data
      if(Array.isArray(r.value?.data)) this.reviews=r.value.data
      if(p.value?.success!==false && p.value?.data) this.coachProfile=p.value.data||{}
      if(Array.isArray(s.value?.data)) this.coachStudents=s.value.data
      if(Array.isArray(f.value?.data)) this.coachFacilities=f.value.data
      if(Array.isArray(fb.value?.data)) this.coachFacilityBookings=fb.value.data
      if(Array.isArray(ts.value?.data)) this.coachSchedule=ts.value.data
    },
    defaultPointGoods(){
      return [
        {id:1,name:'5元无门槛券',nameEn:'¥5 No-threshold Coupon',description:'满0元可用，直减5元',descriptionEn:'Use with no minimum spend, save ¥5',pointsCost:10,category:'优惠券',discountType:'COUPON',icon:'coupon',imageKey:'coupon',stockQuantity:999,soldCount:0,active:true},
        {id:2,name:'10元实付券',nameEn:'¥10 Payment Coupon',description:'实付满100元可用',descriptionEn:'Save ¥10 when spending over ¥100',pointsCost:30,category:'优惠券',discountType:'COUPON',icon:'ticket',imageKey:'ticket',stockQuantity:888,soldCount:3,active:true},
        {id:5,name:'运动水壶',nameEn:'Sports Bottle',description:'大容量运动水壶，便携耐用',descriptionEn:'Large and durable bottle for training',pointsCost:200,category:'运动装备',discountType:'PRODUCT',icon:'bottle',imageKey:'bottle',stockQuantity:100,soldCount:12,active:true},
        {id:6,name:'瑜伽垫',nameEn:'Yoga Mat',description:'加厚防滑瑜伽垫',descriptionEn:'Thick non-slip yoga mat',pointsCost:300,category:'运动装备',discountType:'PRODUCT',icon:'yoga',imageKey:'yoga',stockQuantity:80,soldCount:15,active:true},
        {id:9,name:'VIP专属-蛋白粉',nameEn:'VIP Protein Powder',description:'高蛋白健身蛋白粉',descriptionEn:'High-protein powder for VIP members',pointsCost:500,category:'VIP专属',discountType:'PRODUCT',icon:'protein',imageKey:'protein',stockQuantity:50,soldCount:24,isVip:true,active:true},
        {id:10,name:'VIP专属-智能手环',nameEn:'VIP Smart Band',description:'运动智能手环',descriptionEn:'Smart fitness band for VIP members',pointsCost:800,category:'VIP专属',discountType:'PRODUCT',icon:'band',imageKey:'band',stockQuantity:30,soldCount:27,isVip:true,active:true}
      ]
    },
    defaultClassRows(){
      const today=new Date(); const iso=d=>d.toISOString().slice(0,10); const mk=(id,name,nameEn,coachName,type,dayOffset,hour,capacity,bookedCount,location)=>{ const d=new Date(today); d.setDate(today.getDate()+dayOffset); const date=iso(d); return {id,name,nameEn,coachId:1,coachName,instructor:coachName,type,capacity,bookedCount,location,startTime:`${date}T${String(hour).padStart(2,'0')}:00`,endTime:`${date}T${String(hour+1).padStart(2,'0')}:00`,status:'ACTIVE'} }
      return [
        mk(1,'力量训练','Strength Training','张教练','GROUP',0,9,25,7,'C区力量区'),
        mk(2,'瑜伽放松','Yoga Relaxation','李教练','GROUP',0,11,25,9,'A区教室'),
        mk(3,'动感单车','Spinning','王教练','GROUP',0,13,25,11,'B区动感单车房'),
        mk(101,'一对一力量评估','Private Strength Assessment','张教练','PRIVATE',1,14,1,0,'私教训练室1')
      ]
    },
    pick(obj,key){ if(!obj) return ''; const en=this.lang==='en'; return en && obj[key+'En'] ? obj[key+'En'] : (obj[key] || obj[key.replace('Name','')] || '') },
    avatarGlyph(p){ const s=String((p&& (p.avatar||p.author||p.username)) || ''); if(/yoga|瑜伽/i.test(s)) return '🧘'; if(/run|跑/i.test(s)) return '🏃'; if(/dance|舞/i.test(s)) return '💃'; if(/力|power|gym/i.test(s)) return '力'; if(/健|fit|fan/i.test(s)) return '健'; return s.slice(0,1) || '健' },
    rankAvatar(r){ const s=String((r&& (r.avatar||r.name||r.username)) || ''); if(/yoga|瑜伽/i.test(s)) return '🧘'; if(/run|跑/i.test(s)) return '🏃'; if(/dance|动感|少女/i.test(s)) return '💃'; if(/weight|举重/i.test(s)) return '🏋️'; if(/sports|运动达人/i.test(s)) return '🏅'; if(/star|新星/i.test(s)) return '✨'; if(/power|力量/i.test(s)) return '👤'; return this.iconFor(s) },
    postAuthor(p){ if(!p) return ''; return this.lang==='en' && p.authorEn ? p.authorEn : (p.author || p.username || '') },
    postTime(p){ if(!p) return ''; if(this.lang==='en' && p.timeEn) return p.timeEn; if(p.time) return p.time; return this.prettyDate(p.createdAt) },
    iconFor(v){ const s=String(v||'').toLowerCase(); if(s.includes('yoga')||s.includes('瑜伽')) return '🧘'; if(s.includes('run')||s.includes('跑')) return '🏃'; if(s.includes('bike')||s.includes('单车')) return '🚴'; if(s.includes('protein')||s.includes('蛋白')) return '🥤'; if(s.includes('shirt')||s.includes('t恤')) return '👕'; if(s.includes('vip')) return '💎'; if(s.includes('coupon')||s.includes('券')) return '🎫'; if(s.includes('water')||s.includes('水')) return '💧'; return '💪' },
    spotsLeft(c){ const cap=Number(c.capacity||20); const booked=Number(c.bookedCount||c.participants||0); if(c.spotsLeft!==undefined) return Number(c.spotsLeft); return Math.max(0, cap-booked) },
    timeRange(c){ const s=String(c && c.startTime || '').replace('T',' '); const e=String(c && c.endTime || ''); return s ? s.slice(11,16)+(e?' - '+e.slice(11,16):'') : '' },
    adminTimeRange(c){ const s=String(c && c.startTime || '').replace('T',' '); const e=String(c && c.endTime || '').replace('T',' '); const sd=s?`${Number(s.slice(5,7))}月${Number(s.slice(8,10))}日 ${s.slice(11,16)}`:''; const ed=e?`${Number(e.slice(5,7))}月${Number(e.slice(8,10))}日 ${e.slice(11,16)}`:''; return sd && ed ? `${sd} – ${ed}` : this.timeRange(c) },
    isPrivateAdminClass(c){ const t=String(c?.type||'').toUpperCase(); return t.includes('PRIVATE') || Number(c?.capacity||0)<=1 || String(c?.location||'').includes('私教') },
    classFull(c){ return Number(c?.capacity||0)>0 && Number(c?.bookedCount||0) >= Number(c?.capacity||0) },
    statusLabel(v){ const s=String(v||'ACTIVE').toUpperCase(); if(s==='PENDING') return this.lang==='zh'?'待审核':'Pending review'; if(s==='INACTIVE') return this.lang==='zh'?'未启用':'Inactive'; return this.lang==='zh'?'启用':'Active' },
    productCategoryLabel(v){
      const raw=String(v||'Other')
      const key=raw.toLowerCase().replace(/[\s_-]+/g,'')
      const map={
        coupons:['优惠券','Coupons'], coupon:['优惠券','Coupons'], '优惠券':['优惠券','Coupons'],
        equipment:['运动装备','Equipment'], gear:['运动装备','Equipment'], product:['运动装备','Equipment'], '运动装备':['运动装备','Equipment'],
        trainingaccessories:['训练配件','Training Accessories'], accessories:['训练配件','Training Accessories'], '训练配件':['训练配件','Training Accessories'],
        supplements:['营养补剂','Supplements'], supplement:['营养补剂','Supplements'], '营养补剂':['营养补剂','Supplements'],
        apparel:['运动服饰','Apparel'], clothing:['运动服饰','Apparel'], '运动服饰':['运动服饰','Apparel'],
        sportsaccessories:['运动配件','Sports Accessories'], '运动配件':['运动配件','Sports Accessories'],
        vipexclusive:['VIP专属','VIP Exclusive'], viponly:['VIP专属','VIP Exclusive'], 'vip专属':['VIP专属','VIP Exclusive'],
        memberservices:['会员服务','Member Services'], '会员服务':['会员服务','Member Services'],
        memberprivileges:['会员权益','Member Privileges'], '会员权益':['会员权益','Member Privileges'],
        other:['其他','Other'], '其他':['其他','Other']
      }
      const pair=map[key] || map[raw] || [raw,raw]
      return this.lang==='zh'?pair[0]:pair[1]
    },
    compactTime(c){ const s=this.timeRange(c); return s ? s.replace(' - ','-') : '09:00-10:00' },
    coachTags(coach){ const src=String(coach && (coach.specialtyEn || coach.specialty || '') || ''); const tags=src.split(/[，,]/).map(x=>x.trim()).filter(Boolean); const fallback=this.lang==='zh'?['力量训练','增肌','功能性训练']:['Strength Training','Muscle Gain','Functional Training']; return (tags.length?tags:fallback).slice(0,3) },
    prettyDate(v){ if(!v) return this.lang==='zh'?'刚刚':'just now'; return String(v).replace('T',' ').slice(0,16) },
    classIdOf(item){ const raw=item && (item.classId ?? item.id ?? (item.classes && item.classes.id)); const id=Number(raw); return Number.isFinite(id) ? id : null },
    activeOffset(){ return this.activeDay==='today' ? 0 : Number(String(this.activeDay).replace('d','')) || 0 },
    dateKeyLocal(d){ return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}` },
    activeDateKey(){ const d=new Date(); d.setDate(d.getDate()+this.activeOffset()); return this.dateKeyLocal(d) },
    itemDateKey(item){ const v=(item && (item.classDate || item.date || item.startTime || (item.classes && (item.classes.classDate || item.classes.date || item.classes.startTime)))) || ''; return String(v).slice(0,10) || this.activeDateKey() },
    isVipGood(p){ const s=String((p&& (p.category||p.name||p.nameEn||'')) || '').toLowerCase(); return !!(p&&p.isVip) || s.includes('vip') || s.includes('专属') },
    syncBookings(rows){
      this.bookings=(Array.isArray(rows)?rows:[]).filter(x=>String(x.status||'CONFIRMED').toUpperCase()==='CONFIRMED')
      this.bookedIds=[...new Set(this.bookings.map(x=>this.classIdOf(x)).filter(x=>x!==null))]
      this.dash.bookings=this.bookings.length
    },
    isCourseBooked(item){ const id=this.classIdOf(item); return id!==null && this.bookedIds.includes(id) },
    isBookingBusy(item){ const id=this.classIdOf(item); return id!==null && this.bookingBusyIds.includes(id) },
    setBookingBusy(item,busy){ const id=this.classIdOf(item); if(id===null) return; this.bookingBusyIds=busy ? [...new Set([...this.bookingBusyIds,id])] : this.bookingBusyIds.filter(x=>x!==id) },
    withBookingFlag(item){ const copy={...(item||{})}; copy.isBooked=this.isCourseBooked(copy) || (this.bookedIds.length===0 && copy.isBooked===true); return copy },
    setLocalCourseState(classId, booked){
      const id=Number(classId);
      const apply=c=>{ if(this.classIdOf(c)===id){ c.isBooked=booked; c.bookedCount=Math.max(0,Number(c.bookedCount||0)+(booked?1:-1)); if(c.spotsLeft!==undefined) c.spotsLeft=Math.max(0,Number(c.spotsLeft||0)+(booked?-1:1)); } }
      this.classes.forEach(apply); this.selectedCoachCourses.forEach(apply)
    },
    async toggleClassBooking(c){
      if(this.isBookingBusy(c)) return
      this.setBookingBusy(c,true)
      try{
        if(this.isCourseBooked(c)){
          const out=await this.api(`/api/bookings/class/${c.id}/cancel`,{method:'PUT'})
          if(out.success===false){ this.message=out.message || (this.lang==='zh'?'取消失败':'Cancel failed'); return }
          this.bookedIds=this.bookedIds.filter(x=>x!==Number(c.id))
          this.setLocalCourseState(c.id,false)
          this.message=this.lang==='zh'?'已取消预约':'Booking cancelled'
        } else {
          if(this.spotsLeft(c)<=0){ this.message=this.lang==='zh'?'课程已满':'Class is full'; return }
          const out=await this.api('/api/bookings',{method:'POST',body:JSON.stringify({classId:c.id})})
          if(out.success===false){ this.message=out.message || (this.lang==='zh'?'预约失败':'Booking failed'); return }
          if(!this.bookedIds.includes(Number(c.id))) this.bookedIds.push(Number(c.id))
          this.setLocalCourseState(c.id,true)
          this.message=this.lang==='zh'?'预约成功':'Booked successfully'
        }
        await this.refreshBookingState()
      }finally{
        this.setBookingBusy(c,false)
      }
    },
    async openCoachCourses(coach){
      this.selectedCoach=coach
      const o=await this.api(`/api/trainers/${coach.id}/courses`)
      let rows=Array.isArray(o.data)?o.data:[]
      if(!rows.length){
        rows=(this.classes||[]).filter(c=>String(c.type||'').toUpperCase().includes('PRIVATE') && (String(c.coachId)===String(coach.id) || String(c.coachName||'')===String(coach.name||'')))
      }
      if(!rows.length){
        const start=new Date(); start.setDate(start.getDate()+1); start.setHours(16,0,0,0); const end=new Date(start.getTime()+3600000)
        rows=[{id:'demo-private-'+coach.id,name:'私教体验课',nameEn:'Private Coaching Trial',coachId:coach.id,coachName:coach.name,location:'私教区-'+coach.name,locationEn:'Private Zone - '+coach.name,capacity:1,bookedCount:0,type:'PRIVATE',startTime:start.toISOString().slice(0,19),endTime:end.toISOString().slice(0,19),duration:this.lang==='zh'?'60分钟':'60 min'}]
      }
      this.selectedCoachCourses=rows.map(c=>this.withBookingFlag(c))
    },
    async refreshBookingState(){
      const currentCoach=this.selectedCoach
      await this.loadClasses()
      // Re-apply local booking flags after the fresh booking list is loaded. This avoids
      // old isBooked fields from class payloads overriding the source of truth in bookedIds.
      this.classes=this.classes.map(c=>this.withBookingFlag(c))
      if(currentCoach){ await this.openCoachCourses(currentCoach) }
    },
    async togglePrivateBooking(c){
      if(this.isBookingBusy(c)) return
      if(!c.id || String(c.id).startsWith('demo-private-')){ this.message=this.lang==='zh'?'请先在管理员端添加该教练的私教课':'Please add a private course for this coach in admin first'; return }
      this.setBookingBusy(c,true)
      try{
        if(this.isCourseBooked(c)){
          const out=await this.api(`/api/bookings/class/${c.id}/cancel`,{method:'PUT'})
          if(out.success===false){ this.message=out.message || (this.lang==='zh'?'取消失败':'Cancel failed'); return }
          this.bookedIds=this.bookedIds.filter(x=>x!==Number(c.id))
          this.setLocalCourseState(c.id,false)
          this.message=this.lang==='zh'?'已取消预约':'Booking cancelled'
        }else{
          if(this.spotsLeft(c)<=0){ this.message=this.lang==='zh'?'课程已满':'Class is full'; return }
          const out=await this.api('/api/bookings',{method:'POST',body:JSON.stringify({classId:c.id})})
          if(out.success===false){ this.message=out.message || (this.lang==='zh'?'预约失败':'Booking failed'); return }
          if(!this.bookedIds.includes(Number(c.id))) this.bookedIds.push(Number(c.id))
          this.setLocalCourseState(c.id,true)
          this.message=this.lang==='zh'?'预约成功':'Booked successfully'
        }
        await this.refreshBookingState()
      }finally{
        this.setBookingBusy(c,false)
      }
    },
    async bookPrivate(c){ await this.togglePrivateBooking(c) },
    async book(c){
      if(this.isCourseBooked(c) || this.isBookingBusy(c)) return
      if(this.spotsLeft(c)<=0){ this.message=this.lang==='zh'?'课程已满':'Class is full'; return }
      this.setBookingBusy(c,true)
      try{
        const out=await this.api('/api/bookings',{method:'POST',body:JSON.stringify({classId:c.id})})
        if(out.success===false){ this.message=out.message || (this.lang==='zh'?'预约失败':'Booking failed'); return }
        if(!this.bookedIds.includes(Number(c.id))) this.bookedIds.push(Number(c.id))
        this.setLocalCourseState(c.id,true)
        this.message=this.lang==='zh'?'预约成功':'Booked successfully'
        await this.refreshBookingState()
      }finally{
        this.setBookingBusy(c,false)
      }
    },
    async redeem(p){
      if(this.redeemingId===p.id) return
      this.redeemingId=p.id
      try{
        const out=await this.api(`/api/points/exchange/${p.id}`,{method:'POST',timeout:15000})
        if(out.success===false){ this.message=out.message || (this.lang==='zh'?'兑换失败':'Redeem failed'); return }
        const nextBalance=Number(out.data?.balance ?? out.balance)
        if(Number.isFinite(nextBalance)) this.points=nextBalance
        const stock=Number(p.stockQuantity ?? p.stock ?? 0)
        if(stock>0){ p.stockQuantity=Math.max(0,stock-1); p.stock=p.stockQuantity }
        p.soldCount=Number(p.soldCount||0)+1
        const item=out.data?.exchange || out.exchange
        if(item && !this.exchanges.some(e=>String(e.id)===String(item.id))) this.exchanges=[item,...this.exchanges]
        this.message=this.lang==='zh'?'兑换成功！':'Redeemed successfully!'
        await Promise.allSettled([this.loadPoints(),this.loadGoods()])
      }finally{
        this.redeemingId=null
      }
    },
    async saveProfile(){ await this.api('/api/users/profile',{method:'PUT',body:JSON.stringify(this.profile)}); this.message=this.lang==='zh'?'成功':'Success'; setTimeout(()=>{ if(this.message==='成功'||this.message==='Success') this.message='' },1800) },
    complaintLabel(type){ const zh={course:'课程问题',coach:'教练问题',facility:'场地问题',other:'其他问题'}; const en={course:'Course issue',coach:'Coach issue',facility:'Facility issue',other:'Other'}; return (this.lang==='zh'?zh:en)[type] || type },
    async submitComplaint(){ if(!this.complaintContent.trim()) return; await this.api('/api/complaints',{method:'POST',body:JSON.stringify({type:this.complaintType,title:this.complaintLabel(this.complaintType),content:this.complaintContent})}); this.complaintContent=''; await this.loadUserComplaints(); this.message=this.lang==='zh'?'提交成功':'Submitted' },
    async createPost(){ if(!this.newPost.trim()) return; await this.api('/api/posts',{method:'POST',body:JSON.stringify({content:this.newPost,category:'share'})}); this.newPost=''; await this.loadSocial() },
    openPostDialog(){ this.postForm={content:'',category:'share'}; this.showPostModal=true },
    openTeamDialog(){ this.teamForm={title:'',description:'',category:'running',location:this.lang==='zh'?'FitLife健身房':'FitLife Gym',meetTime:this.lang==='zh'?'周末 09:00':'Weekend 09:00',maxMembers:20}; this.showTeamModal=true },
    async submitPostForm(){ if(!this.postForm.content.trim()){ this.message=this.lang==='zh'?'请输入动态内容':'Please enter content'; return } await this.api('/api/posts',{method:'POST',body:JSON.stringify({content:this.postForm.content,category:this.postForm.category})}); this.showPostModal=false; await this.loadSocial(); this.message=this.lang==='zh'?'发布成功':'Posted successfully' },
    async submitTeamForm(){ if(!this.teamForm.title.trim()){ this.message=this.lang==='zh'?'请输入组队名称':'Please enter team name'; return } const payload={...this.teamForm, description:this.teamForm.description || (this.lang==='zh'?'一起训练，一起进步！':'Train together and improve together!')}; await this.api('/api/teams',{method:'POST',body:JSON.stringify(payload)}); this.showTeamModal=false; await this.loadSocial(); this.message=this.lang==='zh'?'创建成功':'Created successfully' },
    teamButtonText(team){ if(team.createdByCurrentUser) return this.lang==='zh'?'✓ 我创建的':'✓ Created by me'; if(team.joined) return this.lang==='zh'?'退出组队':'Leave Team'; return this.lang==='zh'?'加入组队':'Join Team' },
    teamButtonClass(team){ return team.createdByCurrentUser?'mine':(team.joined?'leave':'join') },
    async toggleTeam(team){
      if(team.createdByCurrentUser) return
      const action=team.joined?'leave':'join'
      const before=!!team.joined
      const beforeCount=Number(team.currentMembers||team.members||0)
      // optimistic UI so the button cannot look dead
      team.joined = action==='join'
      team.currentMembers = Math.max(0, beforeCount + (action==='join'?1:-1))
      team.members = team.currentMembers
      const out=await this.api(`/api/teams/${team.id}/${action}`,{method:'POST'})
      if(out.success===false){
        team.joined=before; team.currentMembers=beforeCount; team.members=beforeCount
        this.message=out.message || (this.lang==='zh'?'操作失败':'Action failed')
        return
      }
      await this.loadSocial()
      this.message= action==='join' ? (this.lang==='zh'?'成功加入组队！':'Joined successfully!') : (this.lang==='zh'?'已退出组队':'Left team')
    },
    togglePin(p){ p.pinned=!p.pinned; this.message=p.pinned ? (this.lang==='zh'?'已置顶':'Pinned') : (this.lang==='zh'?'已取消置顶':'Unpinned') },
    async like(p){
      const beforeLiked=!!p.liked
      const beforeLikes=Number(p.likes||0)
      p.liked=!beforeLiked
      p.likes=Math.max(0,beforeLikes+(p.liked?1:-1))
      const out=await this.api(`/api/posts/${p.id}/like`,{method:'POST'})
      if(out.success===false){
        p.liked=beforeLiked; p.likes=beforeLikes
        this.message=out.message || (this.lang==='zh'?'操作失败':'Action failed')
        return
      }
      const data=out.data||out
      p.likes = Number(out.likes ?? data.likes ?? p.likes ?? 0)
      p.liked = Boolean(data.liked ?? p.liked)
      if(p.liked && !this.likedPosts.includes(p.id)) this.likedPosts.push(p.id)
      if(!p.liked) this.likedPosts=this.likedPosts.filter(id=>id!==p.id)
      await this.loadPoints()
      this.message = p.liked ? (this.lang==='zh'?'点赞成功':'Liked successfully') : (this.lang==='zh'?'已取消点赞':'Like cancelled')
    },
    openReplyDialog(p){ this.replyTarget=p; this.replyText='' },
    async submitReply(){
      if(!this.replyTarget || !this.replyText.trim()){ this.message=this.lang==='zh'?'请输入回复内容':'Please enter a reply'; return }
      const target=this.replyTarget
      const out=await this.api(`/api/posts/${target.id}/comments`,{method:'POST',body:JSON.stringify({content:this.replyText})})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'发送失败':'Send failed'); return }
      const data=out.data||{}
      target.comments=Number(out.comments ?? (target.comments||0)+1)
      if(!Array.isArray(target.replies)) target.replies=[]
      target.replies.push({id:data.id||Date.now(),author:data.author||this.userHandle,username:data.username||this.userHandle,content:data.content||this.replyText,createdAt:data.createdAt||new Date().toISOString()})
      target.replies=target.replies.slice(-3)
      this.replyTarget=null; this.replyText=''
      await this.loadPoints(); this.message=this.lang==='zh'?'发送成功':'Sent successfully'
    },
    async switchRanking(period){
      if(this.rankingPeriod===period) return
      this.rankingPeriod=period
      const old=this.ranking
      await this.loadRanking()
      if(!this.ranking.length && old.length){
        this.ranking=old
        this.message=this.lang==='zh'?'榜单数据加载失败，请稍后重试':'Ranking data failed to load'
      }
    },
    async doCheckin(){
      if(this.checkedToday) return
      const out=await this.api('/api/checkin',{method:'POST',body:'{}'})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'今日已打卡':'Already checked in today'); await this.loadCheckin(); return }
      await this.loadCheckin(); await this.loadPoints(); this.message=this.lang==='zh'?'打卡成功':'Checked in successfully'
    },
    openPaymentModal(){ if(!this.selectedPayment) this.selectedPayment='wechat'; this.showPayModal=true },
    qrPattern(n){ const fixed=[1,2,3,4,5,6,7,12,14,17,19,21,23,24,27,29,31,34,37,38,40,43,45,46,48,50,54,56,58,60,62,63,65,67,69,72,73,76,78,81,83,86,89,91,93,96,97,99,101,104,106,108,111,113,115,116,117,118,119,120,121]; return fixed.includes(n) || ((n*7 + this.selectedPayment.length*11) % 13 < 5) },
    async finishVipPayment(){ if(this.vipProcessing) return; this.vipProcessing=true; const ok=await this.buyVip(); this.vipProcessing=false; if(ok) this.showPayModal=false },
    addPlanToDate(dateStr, plan){
      const now=new Date(); let base=now;
      if(dateStr){ const old=new Date(dateStr+'T00:00:00'); if(!Number.isNaN(old.getTime()) && old>now) base=old }
      const d=new Date(base);
      if(plan==='month') d.setMonth(d.getMonth()+1); else d.setFullYear(d.getFullYear()+1);
      return d.toISOString().slice(0,10)
    },
    async buyVip(){
      const before=this.membershipEndDate
      const out=await this.api('/api/user/membership/purchase-vip',{method:'POST',body:JSON.stringify({plan:this.selectedPlan,paymentMethod:this.selectedPayment})})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'支付失败':'Payment failed'); return false }
      const d=out.data||{}
      this.isVip=true
      this.membershipEndDate=d.endDate || this.addPlanToDate(before,this.selectedPlan)
      await this.loadProfile()
      if(!this.membershipEndDate || this.membershipEndDate===before) this.membershipEndDate=d.endDate || this.addPlanToDate(before,this.selectedPlan)
      this.message=this.lang==='zh'?`VIP充值成功，已延长至 ${this.membershipEndDate}`:`VIP activated successfully. Extended to ${this.membershipEndDate}`
      return true
    },
    openUserDialog(mode,kind,row=null){
      this.adminDialog={mode,kind,row}
      if(mode==='add') this.adminForm={username:'',password:'123456',name:'',email:'',phone:'',membershipType:'标准会员',role:'USER',active:true,newPassword:''}
      else this.adminForm={id:row.id,username:row.username,name:row.name||'',email:row.email||'',phone:row.phone||'',membershipType:row.membershipType||'标准会员',role:row.role||'USER',active:row.active!==false,newPassword:''}
    },
    openClassDialog(mode,row=null){
      const now=new Date(); now.setMinutes(now.getMinutes()-now.getTimezoneOffset()); const start=now.toISOString().slice(0,16); const endDate=new Date(now.getTime()+3600000); endDate.setMinutes(endDate.getMinutes()-endDate.getTimezoneOffset()); const end=endDate.toISOString().slice(0,16)
      this.adminDialog={mode,kind:'class',row}
      if(mode==='add') this.adminForm={name:'',nameEn:'',coachId:this.coaches[0]?String(this.coaches[0].id):'',capacity:this.adminClassTab==='private'?1:25,startTime:start,endTime:end,location:this.adminClassTab==='private'?'私教区':'A区教室',locationEn:this.adminClassTab==='private'?'Private Zone':'Room A',status:'ACTIVE',type:this.adminClassTab==='private'?'PRIVATE':'GROUP'}
      else this.adminForm={id:row.id,name:row.name||'',nameEn:row.nameEn||'',coachId:String(row.coachId||''),capacity:row.capacity||1,startTime:this.toInputDateTime(row.startTime),endTime:this.toInputDateTime(row.endTime),location:row.location||'',locationEn:row.locationEn||'',status:row.status||'ACTIVE',type:this.isPrivateAdminClass(row)?'PRIVATE':'GROUP'}
    },
    closeAdminDialog(){ this.adminDialog=null; this.adminForm={} },
    toInputDateTime(v){ const s=String(v||''); return s.includes('T')?s.slice(0,16):s.replace(' ','T').slice(0,16) },
    selectedCoachName(){ const id=String(this.adminForm.coachId||''); const c=this.coaches.find(x=>String(x.id)===id); return c ? c.name : (this.adminForm.coachName||'张教练') },
    async saveAdminDialog(){
      if(!this.adminDialog) return
      const d=this.adminDialog
      if(d.kind==='member' || d.kind==='user'){
        const body={name:this.adminForm.name,email:this.adminForm.email,phone:this.adminForm.phone,active:this.adminForm.active,membershipType:this.adminForm.membershipType,role:this.adminForm.role||'USER'}
        if(this.adminForm.newPassword) body.password=this.adminForm.newPassword
        if(d.mode==='add') Object.assign(body,{username:this.adminForm.username,password:this.adminForm.password||'123456'})
        const url=d.mode==='add'?'/api/admin/users':`/api/admin/users/${this.adminForm.id}`
        await this.api(url,{method:d.mode==='add'?'POST':'PUT',body:JSON.stringify(body)})
      } else if(d.kind==='class'){
        const body={name:this.adminForm.name,nameEn:this.adminForm.nameEn||this.adminForm.name,coachId:Number(this.adminForm.coachId)||null,coachName:this.selectedCoachName(),capacity:Number(this.adminForm.capacity)||1,startTime:this.adminForm.startTime,endTime:this.adminForm.endTime,location:this.adminForm.location,locationEn:this.adminForm.locationEn||this.adminForm.location,status:this.adminForm.status,type:this.adminForm.type||this.adminClassTab.toUpperCase()}
        const url=d.mode==='add'?'/api/admin/classes':`/api/admin/classes/${this.adminForm.id}`
        await this.api(url,{method:d.mode==='add'?'POST':'PUT',body:JSON.stringify(body)})
      } else if(d.kind==='product'){
        const body={name:this.adminForm.name,nameEn:this.adminForm.nameEn||this.adminForm.name,category:this.adminForm.category,pointsCost:Number(this.adminForm.pointsCost)||0,points:Number(this.adminForm.pointsCost)||0,stockQuantity:Number(this.adminForm.stockQuantity)||0,stock:Number(this.adminForm.stockQuantity)||0,description:this.adminForm.description,descriptionEn:this.adminForm.descriptionEn,imageKey:this.adminForm.imageKey||'gift',icon:this.adminForm.imageKey||'gift',active:this.adminForm.active!==false,isVip:String(this.adminForm.category||'').toLowerCase().includes('vip')}
        const url=d.mode==='add'?'/api/points/admin/products':`/api/points/admin/products/${this.adminForm.id}`
        await this.api(url,{method:d.mode==='add'?'POST':'PUT',body:JSON.stringify(body)})
      } else if(d.kind==='post'){
        const body={category:this.adminForm.category,content:this.adminForm.content,visible:this.adminForm.visible!==false}
        await this.api(`/api/admin/posts/${this.adminForm.id}`,{method:'PUT',body:JSON.stringify(body)})
      }
      this.closeAdminDialog(); await this.loadAdmin(); this.message=this.lang==='zh'?'保存成功':'Saved successfully'
    },
    async editUser(u){ this.openUserDialog('edit', this.route==='/admin/dashboard'?'member':'user', u) },
    async deleteUser(u){ if(confirm(this.lang==='zh'?'确认删除用户？':'Delete this user?')){ await this.api(`/api/admin/users/${u.id}`,{method:'DELETE'}); await this.loadAdmin() } },
    quickAddUser(){ this.openUserDialog('add', this.route==='/admin/dashboard'?'member':'user') },
    quickAddClass(){ this.openClassDialog('add') },
    quickEditClass(c){ this.openClassDialog('edit',c) },
    openProductDialog(mode,row=null){
      this.adminDialog={mode,kind:'product',row}
      if(mode==='add') this.adminForm={name:'',nameEn:'',category:'Equipment',pointsCost:100,stockQuantity:100,description:'',descriptionEn:'',imageKey:'gift',active:true}
      else this.adminForm={id:row.id,name:row.name||'',nameEn:row.nameEn||'',category:row.category||'Equipment',pointsCost:row.pointsCost||row.points||100,stockQuantity:row.stockQuantity||row.stock||100,description:row.description||'',descriptionEn:row.descriptionEn||'',imageKey:row.imageKey||row.icon||'gift',active:row.active!==false}
    },
    openPostDialog(row){ this.adminDialog={mode:'edit',kind:'post',row}; this.adminForm={id:row.id,category:row.category||'share',content:row.content||'',visible:row.visible!==false} },
    adminPostCategoryLabel(v){ const x=String(v||'chat').toLowerCase(); if(x.includes('share')) return '📥 Share'; if(x.includes('help')) return '❓ Help'; if(x.includes('team')) return '👥 Team'; if(x.includes('checkin')) return 'checkin'; return '💬 Chat' },
    reportStatusLabel(v){ const x=String(v||'PENDING').toUpperCase(); if(x.includes('PROCESSED')||x.includes('RESOLVED')) return 'Resolved'; if(x.includes('IGNORED')) return 'Dismissed'; return 'Pending' },
    async quickAddProduct(){ this.openProductDialog('add') },
    async quickEditProduct(p){ this.openProductDialog('edit',p) },
    async adminDeleteClass(c){ if(confirm(this.lang==='zh'?'删除这个课程？':'Delete this class?')){ await this.api(`/api/admin/classes/${c.id}`,{method:'DELETE'}); await this.loadAdmin(); this.message=this.lang==='zh'?'已删除':'Deleted' } },
    async adminDeleteProduct(p){ if(confirm(this.lang==='zh'?'删除这个商品？用户端积分商城也会同步删除。':'Delete this product? It will also disappear from user points mall.')){ await this.api(`/api/points/admin/products/${p.id}`,{method:'DELETE'}); await this.loadAdmin(); this.message=this.lang==='zh'?'商品已同步删除':'Product deleted and synced' } },
    async processAdminReport(r){ await this.api(`/api/admin/reports/${r.id}/process`,{method:'POST'}); await this.loadAdmin(); this.message=this.lang==='zh'?'举报已处理':'Report processed' },
    async ignoreAdminReport(r){ await this.api(`/api/admin/reports/${r.id}/ignore`,{method:'POST'}); await this.loadAdmin(); this.message=this.lang==='zh'?'已忽略举报':'Report ignored' },
    async deletePost(p){ await this.api(`/api/admin/posts/${p.id}`,{method:'DELETE'}); await this.loadAdmin() },
    coachNavLabel(item){
      const map={
        '/coach/dashboard':'Dashboard', '/coach/profile':'My Profile', '/coach/classes':'Classes', '/coach/mailbox':'Coach Mailbox'
      }
      return map[item.path] || this.t(item.key)
    },
    coachClassStatusKey(c){ const end=new Date(c.endTime||c.startTime||0); return (!Number.isNaN(end.getTime()) && end < new Date()) ? 'completed' : 'upcoming' },
    coachClassStatusLabel(c){ return this.coachClassStatusKey(c)==='completed' ? 'Completed' : 'Upcoming' },
    coachClassStatusClass(c){ return this.coachClassStatusKey(c)==='completed' ? 'done' : 'ready' },
    coachDisplayTime(c){ const s=String(c?.startTime||''); const e=String(c?.endTime||''); return s ? `${s.slice(11,16)}-${e.slice(11,16) || ''}` : '09:00-10:00' },
    maskPhone(v){ const s=String(v||''); return s.length>7 ? s.slice(0,3)+'****'+s.slice(-4) : s },
    facilitySlots(f){
      const slots=Array.isArray(f.timeSlots)?f.timeSlots:(Array.isArray(f.slots)?f.slots:['09:00-11:00','14:00-16:00','19:00-21:00'])
      const booked=new Set((this.coachFacilityBookings||[]).filter(b=>String(b.status||'RESERVED').toUpperCase()!=='CANCELLED' && Number(b.facilityId)===Number(f.id)).map(b=>String(b.timeSlot||`${b.startTime}-${b.endTime}`).replace(/\s/g,'')))
      return slots.filter(slot=>!booked.has(String(slot).replace(/\s/g,'')))
    },
    pickFacilityBooking(b){ return `${this.lang==='en' && b.facilityNameEn ? b.facilityNameEn : (b.facilityName||b.location||'Facility')} · ${b.date||''} ${b.startTime||''}–${b.endTime||''}` },
    isFacilitySlotSelected(f,slot){
      return !!this.coachPendingFacility && Number(this.coachPendingFacility.facilityId)===Number(f.id) && this.coachPendingFacility.slot===slot
    },
    selectFacilitySlot(f,slot){
      if(this.isFacilitySlotSelected(f,slot)){ this.clearFacilitySlot(); return }
      this.coachPendingFacility={facilityId:f.id, slot}
    },
    clearFacilitySlot(){ this.coachPendingFacility=null },
    async confirmFacilitySlot(f,slot){
      const date=this.dateKeyLocal(new Date())
      const [startTime,endTime]=String(slot).split('-')
      const tempId='tmp-'+Date.now()
      const tempBooking={id:tempId,facilityId:f.id,facilityName:f.name,facilityNameEn:f.nameEn,date,startTime,endTime,timeSlot:slot,status:'RESERVED',usedByClass:false}
      this.coachPendingFacility=null
      this.coachFacilityBookings=[...(this.coachFacilityBookings||[]), tempBooking]
      this.message=this.lang==='zh'?'场地预约处理中...':'Booking facility...'
      const out=await this.api('/api/coach/facilities/book',{method:'POST',body:JSON.stringify({facilityId:f.id,timeSlot:slot,date})})
      if(out.success===false){
        this.coachFacilityBookings=(this.coachFacilityBookings||[]).filter(b=>b.id!==tempId)
        this.message=out.message || (this.lang==='zh'?'预约失败':'Booking failed')
        return
      }
      this.message=this.lang==='zh'?'场地预约成功':'Facility booked successfully'
      await this.loadCoach()
    },
    async bookFacilitySlot(f,slot){ return this.confirmFacilitySlot(f,slot) },
    async cancelFacilityBooking(b){
      if(!confirm(this.lang==='zh'?'删除该场地预约？删除后该时段会重新可预约。':'Delete this facility booking? The time slot will become available again.')) return
      const out=await this.api(`/api/coach/facilities/bookings/${b.id}`,{method:'DELETE'})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'删除失败':'Delete failed'); return }
      this.message=this.lang==='zh'?'已删除场地预约':'Facility booking deleted'
      await this.loadCoach()
    },
    resetCoachClassForm(){
      if(!this.coachClassForm || this.coachClassForm.name) this.coachClassForm={name:'',date:'',startTime:'',endTime:'',reservedBookingId:'',location:'',capacity:20,description:''}
    },
    chooseReservedFacility(){
      const b=(this.coachFacilityBookings||[]).find(x=>String(x.id)===String(this.coachClassForm.reservedBookingId))
      if(!b) return
      this.coachClassForm.date=b.date||''
      this.coachClassForm.startTime=b.startTime||''
      this.coachClassForm.endTime=b.endTime||''
      this.coachClassForm.location=this.lang==='en' && b.facilityNameEn ? b.facilityNameEn : (b.facilityName||'')
    },
    async submitCoachClass(){
      const f=this.coachClassForm
      if(!f.name.trim()){ this.message=this.lang==='zh'?'请输入课程名称':'Please enter class name'; return }
      if(!f.reservedBookingId){ this.message=this.lang==='zh'?'请先选择已预约场地':'Please select a booked facility first'; return }
      const body={name:f.name,nameEn:f.name,date:f.date,startTime:f.startTime,endTime:f.endTime,location:f.location,locationEn:f.location,capacity:Number(f.capacity)||20,description:f.description,descriptionEn:f.description,type:'GROUP',reservedBookingId:Number(f.reservedBookingId)}
      const out=await this.api('/api/coach/classes',{method:'POST',body:JSON.stringify(body)})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'添加失败':'Add failed'); return }
      this.message=this.lang==='zh'?'课程已添加':'Class added'
      this.coachSubTab='mine'; this.resetCoachClassForm(); await this.loadCoach()
    },
    openCoachEdit(c){
      this.coachEditDialog={id:c.id,name:c.name||'',nameEn:c.nameEn||'',date:this.itemDateKey(c),startTime:String(c.startTime||'').slice(11,16),endTime:String(c.endTime||'').slice(11,16),location:c.location||'',capacity:Number(c.capacity||20)}
    },
    async saveCoachEdit(){
      const f=this.coachEditDialog; if(!f) return
      const body={name:f.name,nameEn:f.nameEn||f.name,startTime:`${f.date}T${f.startTime}`,endTime:`${f.date}T${f.endTime}`,location:f.location,locationEn:f.location,capacity:Number(f.capacity)||20}
      const out=await this.api(`/api/coach/classes/${f.id}`,{method:'PUT',body:JSON.stringify(body)})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'保存失败':'Save failed'); return }
      this.coachEditDialog=null; this.message=this.lang==='zh'?'保存成功':'Saved successfully'; await this.loadCoach()
    },
    async saveCoachSettings(){ await this.api('/api/coach/notification-settings',{method:'POST',body:JSON.stringify(this.coachNotify)}); this.message=this.lang==='zh'?'保存成功':'Saved successfully' },
    async quickCoachClass(){ const name=prompt(this.lang==='zh'?'课程名称':'Class name'); if(name){ await this.api('/api/coach/classes',{method:'POST',body:JSON.stringify({name,nameEn:name,date:new Date().toISOString().slice(0,10),startTime:'10:00',endTime:'11:00',location:'A区教室',locationEn:'Room A',capacity:20})}); await this.loadCoach(); this.message=this.lang==='zh'?'课程已发布':'Class added' } },
    async coachDeleteClass(c){ if(confirm(this.lang==='zh'?'删除这个课程？':'Delete this class?')){ await this.api(`/api/coach/classes/${c.id}`,{method:'DELETE'}); await this.loadCoach(); this.message=this.lang==='zh'?'已删除':'Deleted' } },
    async saveCoachProfile(){ this.message=this.lang==='zh'?'保存成功':'Saved successfully' },
    openCoachComplaint(c){ this.coachMailDialog={kind:'complaint',row:c}; this.coachMailReply=c.response || (this.lang==='zh'?'已收到你的反馈，我们会调整课程安排并持续跟进。':'Feedback received. We will adjust the class arrangement and keep following up.') },
    openCoachReview(r){ this.coachMailDialog={kind:'review',row:r}; this.coachMailReply=r.response || (this.lang==='zh'?'感谢你的评价，我会继续优化训练指导。':'Thank you for your review. I will keep improving the training guidance.') },
    async submitCoachMailReply(){
      if(!this.coachMailDialog) return
      if(!String(this.coachMailReply||'').trim()){ this.message=this.lang==='zh'?'请输入回复内容':'Please enter a reply'; return }
      const row=this.coachMailDialog.row
      const isComplaint=this.coachMailDialog.kind==='complaint'
      const url=isComplaint ? `/api/coach/complaints/${row.id}/respond` : `/api/coach/reviews/${row.id}/respond`
      const out=await this.api(url,{method:'POST',body:JSON.stringify({response:this.coachMailReply,responseEn:this.coachMailReply,reply:this.coachMailReply})})
      if(out.success===false){ this.message=out.message || (this.lang==='zh'?'提交失败':'Submit failed'); return }
      this.coachMailDialog=null; this.coachMailReply=''; this.message=isComplaint ? (this.lang==='zh'?'投诉已处理':'Complaint handled') : (this.lang==='zh'?'评价已回复':'Review replied')
      await this.loadCoach()
    },
    async respondComplaint(c){ this.openCoachComplaint(c) },
    async respondReview(r){ this.openCoachReview(r) }
  }
})
</script>
