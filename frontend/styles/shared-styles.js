const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<custom-style>  
  <style>
    html {
      --vaadin-app-layout-drawer-width: 13em;
    }

    #view-layout-laboratory {
      background-color: var(--lumo-base-color);
      color: var(--lumo-success-text-color);
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

