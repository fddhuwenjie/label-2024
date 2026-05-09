<template>
  <div class="home">
    <!-- Hero Section -->
    <section class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-badge">专业心理咨询平台</div>
        <h1>让心灵找到<span class="gradient-text">归属</span></h1>
        <p class="hero-desc">
          汇聚国内顶尖心理咨询师，为您提供专业、私密、温暖的心理健康服务。
          无论您面临什么困扰，我们都在这里倾听与陪伴。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" class="primary-btn" @click="$router.push('/counselors')">
            立即预约咨询
            <el-icon class="btn-arrow"><ArrowRight /></el-icon>
          </el-button>
          <el-button size="large" class="secondary-btn" @click="scrollToFeatures">
            了解更多
          </el-button>
        </div>
        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-number">500+</span>
            <span class="stat-label">认证咨询师</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">50000+</span>
            <span class="stat-label">服务用户</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-number">98%</span>
            <span class="stat-label">好评率</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features" id="features">
      <div class="section-header">
        <h2>为什么选择我们</h2>
        <p>我们致力于提供最优质的心理健康服务体验</p>
      </div>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" fill="currentColor"/>
            </svg>
          </div>
          <h3>专业认证</h3>
          <p>所有咨询师均持有国家认证资质，平均从业经验超过8年</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon purple">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z" fill="currentColor"/>
            </svg>
          </div>
          <h3>隐私保护</h3>
          <p>采用银行级加密技术，严格保护您的个人信息和咨询内容</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon green">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z" fill="currentColor"/>
            </svg>
          </div>
          <h3>灵活预约</h3>
          <p>7×24小时在线预约，支持多种咨询方式，随时随地获得帮助</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon orange">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" fill="currentColor"/>
            </svg>
          </div>
          <h3>温暖陪伴</h3>
          <p>不只是咨询，更是心灵的陪伴，帮助您找到内心的力量</p>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta">
      <div class="cta-content">
        <h2>准备好开始您的心灵之旅了吗？</h2>
        <p>专业咨询师在线等候，为您提供一对一专属服务</p>
        <el-button type="primary" size="large" class="cta-btn" @click="$router.push('/counselors')">
          浏览咨询师
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
const scrollToFeatures = () => {
  document.getElementById('features')?.scrollIntoView({ behavior: 'smooth' })
}
</script>

<style scoped>
.home {
  background: #f8fafc;
}

/* Hero Section */
.hero {
  position: relative;
  padding: 100px 32px 120px;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.hero-bg::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 800px;
  height: 800px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 60%);
  border-radius: 50%;
}

.hero-bg::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 60%);
  border-radius: 50%;
}

.hero-content {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  color: white;
}

.hero-badge {
  display: inline-block;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 100px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 32px;
  backdrop-filter: blur(10px);
}

.hero h1 {
  font-size: 64px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 24px;
  letter-spacing: -2px;
}

.gradient-text {
  background: linear-gradient(135deg, #a5b4fc 0%, #c4b5fd 50%, #f0abfc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-desc {
  font-size: 20px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 48px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 80px;
}

.primary-btn {
  height: 56px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;
}

.primary-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 40px rgba(99, 102, 241, 0.4);
}

.btn-arrow {
  transition: transform 0.2s;
}

.primary-btn:hover .btn-arrow {
  transform: translateX(4px);
}

.secondary-btn {
  height: 56px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.hero-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 48px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

/* Features Section */
.features {
  padding: 120px 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-header h2 {
  font-size: 40px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 16px;
}

.section-header p {
  font-size: 18px;
  color: #64748b;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 36px 28px;
  transition: all 0.3s;
  border: 1px solid #e2e8f0;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.08);
  border-color: transparent;
}

.feature-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #6366f1 0%, #818cf8 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  color: white;
}

.feature-icon svg {
  width: 28px;
  height: 28px;
}

.feature-icon.purple {
  background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
}

.feature-icon.green {
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
}

.feature-icon.orange {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
}

.feature-card h3 {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 12px;
}

.feature-card p {
  font-size: 15px;
  line-height: 1.6;
  color: #64748b;
}

/* CTA Section */
.cta {
  padding: 100px 32px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
}

.cta-content {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
  color: white;
}

.cta h2 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
}

.cta p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 32px;
}

.cta-btn {
  height: 56px;
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 14px;
  background: white;
  color: #6366f1;
  border: none;
}

.cta-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(0, 0, 0, 0.2);
  background: white;
  color: #6366f1;
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 60px 20px 80px;
  }
  .hero h1 {
    font-size: 36px;
    letter-spacing: -1px;
  }
  .hero-desc {
    font-size: 15px;
    padding: 0 10px;
  }
  .hero-badge {
    font-size: 13px;
    padding: 6px 16px;
  }
  .hero-actions {
    flex-direction: column;
    gap: 12px;
    margin-bottom: 60px;
  }
  .primary-btn, .secondary-btn {
    width: 100%;
    height: 50px;
  }
  .hero-stats {
    flex-direction: column;
    gap: 20px;
  }
  .stat-divider {
    width: 40px;
    height: 1px;
  }
  .stat-number {
    font-size: 28px;
  }
  .features {
    padding: 60px 20px;
  }
  .features-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  .feature-card {
    padding: 28px 24px;
  }
  .section-header h2 {
    font-size: 26px;
  }
  .section-header p {
    font-size: 15px;
  }
  .cta {
    padding: 60px 20px;
  }
  .cta h2 {
    font-size: 24px;
  }
  .cta p {
    font-size: 15px;
  }
  .cta-btn {
    width: 100%;
    height: 50px;
  }
}

@media (max-width: 480px) {
  .hero h1 {
    font-size: 28px;
  }
  .hero-desc {
    font-size: 14px;
  }
  .stat-number {
    font-size: 24px;
  }
  .stat-label {
    font-size: 12px;
  }
  .section-header h2 {
    font-size: 22px;
  }
  .feature-card h3 {
    font-size: 18px;
  }
  .feature-card p {
    font-size: 14px;
  }
}
</style>
