const $_documentContainer = document.createElement('template');

$_documentContainer.innerHTML = `<custom-style>  
  <style>
    .tags-field vaadin-button {
        border-radius: 25px;
        color: var(--lumo-secondary-text-color);
    }
  </style> 
 </custom-style>`;

document.head.appendChild($_documentContainer.content);

