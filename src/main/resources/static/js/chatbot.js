// ══════════════════════════════════════════════════════════════
//  MicroBizMax Chatbot Widget – Gemini-powered support assistant
// ══════════════════════════════════════════════════════════════

(function () {
    // ── HTML ──────────────────────────────────────────────────────
    const html = `
    <button id="chatbot-fab" aria-label="Open MicroBizMax Assistant">
        <span>💬</span>
        <span class="fab-badge" id="chatBadge">1</span>
    </button>

    <div id="chatbot-window" role="dialog" aria-label="MicroBizMax Support Chat">
        <div class="chat-header">
            <div class="chat-avatar">🤖</div>
            <div class="chat-header-info">
                <div class="chat-header-title">MicroBizMax Assistant</div>
                <div class="chat-header-status">
                    <span class="status-dot"></span>
                    <span>Online · Powered by Gemini AI</span>
                </div>
            </div>
            <button class="chat-close-btn" id="chatCloseBtn" aria-label="Close chat">✕</button>
        </div>

        <div class="chat-body" id="chatBody"></div>

        <div class="chat-suggestions" id="chatSuggestions">
            <button class="suggestion-chip" onclick="chatSuggest('How do I login?')">🔐 Login help</button>
            <button class="suggestion-chip" onclick="chatSuggest('How to add a customer?')"> Add customer</button>
            <button class="suggestion-chip" onclick="chatSuggest('How to add a product?')"> Add product</button>
            <button class="suggestion-chip" onclick="chatSuggest('How to create a sale?')"> New sale</button>
            <button class="suggestion-chip" onclick="chatSuggest('How do discounts work?')"> Discounts</button>
        </div>

        <div class="chat-input-bar">
            <textarea id="chatInput" placeholder="Ask me anything about MicroBizMax…" rows="1" maxlength="500"></textarea>
            <button id="chatSendBtn" aria-label="Send message">➤</button>
        </div>
    </div>`;

    // Inject into page
    const wrapper = document.createElement('div');
    wrapper.innerHTML = html;
    document.body.appendChild(wrapper);

    // Inject CSS if not already present
    if (!document.querySelector('link[href="/css/chatbot.css"]')) {
        const link = document.createElement('link');
        link.rel = 'stylesheet'; link.href = '/css/chatbot.css';
        document.head.appendChild(link);
    }

    // ── State ─────────────────────────────────────────────────────
    const fab    = document.getElementById('chatbot-fab');
    const win    = document.getElementById('chatbot-window');
    const body   = document.getElementById('chatBody');
    const input  = document.getElementById('chatInput');
    const sendBtn = document.getElementById('chatSendBtn');
    const badge  = document.getElementById('chatBadge');
    const closeBtn = document.getElementById('chatCloseBtn');
    const suggestions = document.getElementById('chatSuggestions');
    let isOpen = false;

    // ── Welcome message ───────────────────────────────────────────
    function addWelcome() {
        addBotMessage(" Hi! I'm your **MicroBizMax Assistant**, powered by Gemini AI.\n\nI can help you with:\n• Login & registration\n• Adding customers, products, or sales\n• Applying discounts\n• Managing your profile\n\nWhat would you like help with?");
    }

    // ── Toggle window ─────────────────────────────────────────────
    function toggleChat() {
        isOpen = !isOpen;
        win.classList.toggle('open', isOpen);
        fab.querySelector('span:first-child').textContent = isOpen ? '✕' : '💬';
        if (isOpen) {
            badge.style.display = 'none';
            if (!body.hasChildNodes()) addWelcome();
            setTimeout(() => input.focus(), 300);
        } else {
            fab.querySelector('span:first-child').textContent = '💬';
        }
    }

    fab.addEventListener('click', toggleChat);
    closeBtn.addEventListener('click', toggleChat);

    // ── Add messages ──────────────────────────────────────────────
    function addUserMessage(text) {
        const div = document.createElement('div');
        div.className = 'msg user';
        div.innerHTML = `<div class="msg-bubble">${escHtml(text)}</div>`;
        body.appendChild(div);
        scrollBottom();
        hideSuggestions();
    }

    function addBotMessage(text) {
        const div = document.createElement('div');
        div.className = 'msg bot';
        div.innerHTML = `<div class="msg-bubble">${formatMd(text)}</div>`;
        body.appendChild(div);
        scrollBottom();
    }

    function showTyping() {
        const div = document.createElement('div');
        div.className = 'msg bot'; div.id = 'typingMsg';
        div.innerHTML = `<div class="msg-bubble" style="padding:14px 16px;">
            <div class="typing-indicator">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
            </div>
        </div>`;
        body.appendChild(div);
        scrollBottom();
    }

    function removeTyping() {
        const t = document.getElementById('typingMsg');
        if (t) t.remove();
    }

    function hideSuggestions() {
        if (suggestions) suggestions.style.display = 'none';
    }

    function scrollBottom() {
        setTimeout(() => { body.scrollTop = body.scrollHeight; }, 50);
    }

    // ── Markdown formatter (minimal) ──────────────────────────────
    function formatMd(text) {
        return escHtml(text)
            .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
            .replace(/\*(.+?)\*/g, '<em>$1</em>')
            .replace(/`(.+?)`/g, '<code style="background:rgba(201,149,44,0.15);padding:1px 6px;border-radius:4px;font-size:.82em">$1</code>')
            .replace(/• /g, '• ')
            .replace(/\n/g, '<br>');
    }

    function escHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    // ── Send message ──────────────────────────────────────────────
    async function sendMessage(text) {
        text = (text || input.value).trim();
        if (!text) return;

        input.value = '';
        input.style.height = 'auto';
        sendBtn.disabled = true;

        addUserMessage(text);
        showTyping();

        try {
            const res = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ message: text })
            });
            const data = await res.json();
            removeTyping();
            addBotMessage(data.reply || "I'm sorry, I didn't understand that. Could you rephrase?");
        } catch (e) {
            removeTyping();
            addBotMessage(" I'm having trouble connecting right now. Please try again shortly.");
        } finally {
            sendBtn.disabled = false;
            input.focus();
        }
    }

    // Suggestion chips
    window.chatSuggest = function (text) {
        sendMessage(text);
    };

    // ── Event Listeners ───────────────────────────────────────────
    sendBtn.addEventListener('click', () => sendMessage());

    input.addEventListener('keydown', e => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    // Auto-resize textarea
    input.addEventListener('input', () => {
        input.style.height = 'auto';
        input.style.height = Math.min(input.scrollHeight, 100) + 'px';
    });

    // Close on Escape
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && isOpen) toggleChat();
    });
})();