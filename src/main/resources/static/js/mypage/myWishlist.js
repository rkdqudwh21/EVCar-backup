'use strict';

document.addEventListener('DOMContentLoaded', async () => {
    const wishlistGrid = document.getElementById('wishlistGrid');
    const wishlistEmpty = document.getElementById('wishlistEmpty');
    const wishlistMoreWrap = document.getElementById('wishlistMoreWrap');
    const wishlistMoreButton = document.getElementById('wishlistMoreButton');

    if (!wishlistGrid || !wishlistEmpty || !wishlistMoreWrap || !wishlistMoreButton) {
        return;
    }

    let items = [];
    let visibleCount = 6;

    const formatPrice = (price) => {
        if (price == null) {
            return '가격 정보 없음';
        }
        return `${Number(price).toLocaleString('ko-KR')}만원`;
    };

    const createCard = (item) => {
        const article = document.createElement('article');
        article.className = 'ev-wishlist-card';

        const imageSrc = item.imageUrl && item.imageUrl.trim() !== ''
            ? item.imageUrl
            : '/images/evcar_logo.png';

        article.innerHTML = `
            <div class="ev-wishlist-card__image-wrap">
                <img class="ev-wishlist-card__image" src="${imageSrc}" alt="${item.modelName}">
            </div>

            <div class="ev-wishlist-card__body">
                <p class="ev-wishlist-card__brand">${item.brand ?? ''}</p>
                <h3 class="ev-wishlist-card__title">${item.modelName ?? ''}</h3>
                <p class="ev-wishlist-card__class">${item.vehicleClass ?? ''}</p>
                <p class="ev-wishlist-card__price">${formatPrice(item.priceBasic)}</p>

                <div class="ev-wishlist-card__actions">
                    <a class="btn btn-ev-primary" href="${item.detailUrl ?? '#'}">상세보기</a>
                    <button type="button"
                            class="btn btn-ev-danger"
                            data-wishlist-id="${item.wishlistId}">
                        삭제
                    </button>
                </div>
            </div>
        `;

        const deleteButton = article.querySelector('[data-wishlist-id]');
        deleteButton.addEventListener('click', async () => {
            const wishlistId = deleteButton.getAttribute('data-wishlist-id');

            if (!wishlistId) {
                alert('관심차량 식별값이 없습니다.');
                return;
            }

            if (!confirm('관심 차량에서 삭제하시겠습니까?')) {
                return;
            }

            try {
                const response = await fetch('/mypage/wishlist/delete', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
                    },
                    body: new URLSearchParams({ wishlistId })
                });

                if (!response.ok) {
                    throw new Error('삭제 요청 실패');
                }

                items = items.filter((wishlistItem) => wishlistItem.wishlistId !== wishlistId);
                render();
            } catch (error) {
                console.error(error);
                alert('관심 차량 삭제 중 오류가 발생했습니다.');
            }
        });

        return article;
    };

    const render = () => {
        wishlistGrid.innerHTML = '';

        if (!items.length) {
            wishlistEmpty.classList.remove('ev-wishlist-empty--hidden');
            wishlistMoreWrap.classList.add('ev-wishlist-more--hidden');
            return;
        }

        wishlistEmpty.classList.add('ev-wishlist-empty--hidden');

        const visibleItems = items.slice(0, visibleCount);
        visibleItems.forEach((item) => {
            wishlistGrid.appendChild(createCard(item));
        });

        if (items.length > visibleCount) {
            wishlistMoreWrap.classList.remove('ev-wishlist-more--hidden');
        } else {
            wishlistMoreWrap.classList.add('ev-wishlist-more--hidden');
        }
    };

    wishlistMoreButton.addEventListener('click', () => {
        visibleCount += 6;
        render();
    });

    try {
        const response = await fetch('/mypage/wishlist/api');
        if (!response.ok) {
            throw new Error('목록 조회 실패');
        }

        items = await response.json();
        render();
    } catch (error) {
        console.error(error);
        wishlistGrid.innerHTML = '';
        wishlistEmpty.classList.remove('ev-wishlist-empty--hidden');
        wishlistMoreWrap.classList.add('ev-wishlist-more--hidden');
    }
});