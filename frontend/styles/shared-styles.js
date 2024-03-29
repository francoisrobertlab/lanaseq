/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<custom-style>  
  <style>
    .main-layout {
        padding: 20px;
    }
    
    /* Font sizes */
    .font-size-xxxl {
      font-size: var(--lumo-font-size-xxxl);
    }
    .font-size-xxl {
      font-size: var(--lumo-font-size-xxl);
    }
    .font-size-xl {
      font-size: var(--lumo-font-size-xl);
    }
    .font-size-l {
      font-size: var(--lumo-font-size-l);
    }
    .font-size-m {
      font-size: var(--lumo-font-size-m);
    }
    .font-size-s {
      font-size: var(--lumo-font-size-s);
    }
    .font-size-xs {
      font-size: var(--lumo-font-size-xs);
    }
    .font-size-xxs {
      font-size: var(--lumo-font-size-xxs);
    }
    
    /* Errors */
    .error-text {
      color: var(--lumo-error-text-color);
    }
    .error-10pct {
      background-color: var(--lumo-error-color-10pct);
      color: var(--lumo-error-text-color);
    }
    .error-50pct {
      background-color: var(--lumo-error-color-50pct);
      color: var(--lumo-error-contrast-color);
    }
    .error {
      background-color: var(--lumo-error-color);
      color: var(--lumo-error-contrast-color);
    }
    
    /* Ordered layout with borders */
    vaadin-vertical-layout.border, vaadin-horizontal-layout.border {
      border: 1px solid var(--lumo-contrast-40pct);
      border-radius: 5px;
    }
  </style> 
 </custom-style>`;

document.head.appendChild($_documentContainer.content);

