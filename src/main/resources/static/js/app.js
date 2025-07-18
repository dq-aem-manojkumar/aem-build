// Sidebar toggle functionality
document.addEventListener('DOMContentLoaded', function() {
    const sidebarToggle = document.getElementById('sidebarToggle');
    const sidebar = document.querySelector('.sidebar');
    
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
        
        // Close sidebar when clicking outside on mobile
        document.addEventListener('click', function(event) {
            if (window.innerWidth <= 768) {
                if (!sidebar.contains(event.target) && !sidebarToggle.contains(event.target)) {
                    sidebar.classList.remove('active');
                }
            }
        });
    }
    
    // Form handling for project generator
    const generatorForm = document.querySelector('.generator-form');
    if (generatorForm) {
        generatorForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            // Get form data
            const formData = new FormData(generatorForm);
            const projectData = {
                projectType: document.getElementById('projectType').value,
                aemVersion: document.getElementById('aemVersion').value,
                groupId: document.getElementById('groupId').value,
                artifactId: document.getElementById('artifactId').value,
                version: document.getElementById('version').value,
                packageName: document.getElementById('packageName').value,
                appTitle: document.getElementById('appTitle').value
            };
            
            // Simulate project generation
            const submitButton = generatorForm.querySelector('button[type="submit"]');
            const originalText = submitButton.innerHTML;
            
            submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Generating...';
            submitButton.disabled = true;
            
            setTimeout(() => {
                alert('Project generated successfully! Download will start shortly.');
                submitButton.innerHTML = originalText;
                submitButton.disabled = false;
            }, 2000);
        });
    }
    
    // Auto-fill package name based on group ID and artifact ID
    const groupIdInput = document.getElementById('groupId');
    const artifactIdInput = document.getElementById('artifactId');
    const packageNameInput = document.getElementById('packageName');
    
    if (groupIdInput && artifactIdInput && packageNameInput) {
        function updatePackageName() {
            const groupId = groupIdInput.value;
            const artifactId = artifactIdInput.value;
            
            if (groupId && artifactId) {
                const packageName = groupId + '.' + artifactId.replace(/-/g, '');
                packageNameInput.value = packageName;
            }
        }
        
        groupIdInput.addEventListener('input', updatePackageName);
        artifactIdInput.addEventListener('input', updatePackageName);
    }
    
    // Search functionality
    const searchInput = document.querySelector('.search-box input');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            const projectItems = document.querySelectorAll('.project-item');
            
            projectItems.forEach(item => {
                const title = item.querySelector('h3').textContent.toLowerCase();
                const description = item.querySelector('p').textContent.toLowerCase();
                
                if (title.includes(searchTerm) || description.includes(searchTerm)) {
                    item.style.display = 'flex';
                } else {
                    item.style.display = 'none';
                }
            });
        });
    }
    
    // Smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Add loading states to buttons
    document.querySelectorAll('.btn').forEach(button => {
        button.addEventListener('click', function(e) {
            if (this.classList.contains('btn-primary') && this.type !== 'button') {
                const originalText = this.innerHTML;
                this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';
                this.disabled = true;
                
                setTimeout(() => {
                    this.innerHTML = originalText;
                    this.disabled = false;
                }, 1000);
            }
        });
    });
});