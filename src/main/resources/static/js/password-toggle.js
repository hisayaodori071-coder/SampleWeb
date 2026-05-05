(function () {
    const CLASSES = {
        wrapper: 'js-password-toggle-wrapper',
        input: 'js-password-toggle-input',
        button: 'js-password-toggle-button',
        icon: 'js-password-toggle-icon'
    };

    const EYE_ICON_PATH =
        'M12 5c-5 0-9 4.5-10 7 1 2.5 5 7 10 7s9-4.5 10-7c-1-2.5-5-7-10-7zm0 11a4 4 0 1 1 0-8 4 4 0 0 1 0 8zm0-2a2 2 0 1 0 0-4 2 2 0 0 0 0 4z';

    const EYE_OFF_ICON_PATH =
        'M2 4.27 3.28 3 21 20.73 19.73 22l-3.1-3.1A12.38 12.38 0 0 1 12 19c-5 0-9-4.5-10-7a13.3 13.3 0 0 1 3.38-4.63L2 4.27zM7.53 9.52A4.95 4.95 0 0 0 7 12a5 5 0 0 0 5 5c.87 0 1.69-.22 2.4-.62l-1.74-1.74A2.97 2.97 0 0 1 12 15a3 3 0 0 1-3-3c0-.23.03-.45.08-.66L7.53 9.52zM12 5c5 0 9 4.5 10 7a13.4 13.4 0 0 1-3.07 4.3l-2.14-2.14c.14-.46.21-.94.21-1.44a5 5 0 0 0-5-5c-.5 0-.98.07-1.44.21L8.9 6.27A12.28 12.28 0 0 1 12 5z';

    function addStyles() {
        if (document.getElementById('password-toggle-style')) {
            return;
        }

        const style = document.createElement('style');
        style.id = 'password-toggle-style';
        style.textContent = `
            .${CLASSES.wrapper} {
                position: relative;
                width: 100%;
                max-width: 440px;
            }

            .${CLASSES.input} {
                width: 100%;
                max-width: 100%;
                padding-right: 46px;
            }

            .${CLASSES.button} {
                position: absolute;
                top: 50%;
                right: 10px;
                width: 34px;
                height: 34px;
                transform: translateY(-50%);
                border: none;
                background: transparent;
                color: #607d8b;
                border-radius: 50%;
                cursor: pointer;
                display: inline-flex;
                align-items: center;
                justify-content: center;
                padding: 0;
            }

            .${CLASSES.button}:hover {
                background-color: #eef3f6;
                color: #1976d2;
            }

            .${CLASSES.button}:focus {
                outline: 2px solid #90caf9;
                outline-offset: 2px;
            }

            .${CLASSES.icon} {
                width: 22px;
                height: 22px;
                fill: currentColor;
            }

            .${CLASSES.button}.is-visible {
                color: #1976d2;
                background-color: #e3f2fd;
            }
        `;

        document.head.appendChild(style);
    }

    function createIcon(path) {
        const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('class', CLASSES.icon);
        svg.setAttribute('viewBox', '0 0 24 24');
        svg.setAttribute('aria-hidden', 'true');

        const iconPath = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        iconPath.setAttribute('d', path);

        svg.appendChild(iconPath);
        return svg;
    }

    function setupPasswordToggle(input) {
        if (input.dataset.passwordToggleReady === 'true') {
            return;
        }

        input.dataset.passwordToggleReady = 'true';
        input.classList.add(CLASSES.input);

        const wrapper = document.createElement('div');
        wrapper.className = CLASSES.wrapper;

        input.parentNode.insertBefore(wrapper, input);
        wrapper.appendChild(input);

        const button = document.createElement('button');
        button.type = 'button';
        button.className = CLASSES.button;
        button.setAttribute('aria-label', 'パスワードを表示');

        button.appendChild(createIcon(EYE_ICON_PATH));
        wrapper.appendChild(button);

        button.addEventListener('click', function () {
            const isHidden = input.type === 'password';

            input.type = isHidden ? 'text' : 'password';
            button.classList.toggle('is-visible', isHidden);
            button.setAttribute('aria-label', isHidden ? 'パスワードを非表示' : 'パスワードを表示');

            button.innerHTML = '';
            button.appendChild(createIcon(isHidden ? EYE_OFF_ICON_PATH : EYE_ICON_PATH));
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        addStyles();

        const passwordInputs = document.querySelectorAll('input[type="password"]');
        passwordInputs.forEach(setupPasswordToggle);
    });
})();