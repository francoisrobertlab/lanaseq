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

package ca.qc.ircm.lana.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

/**
 * Save event.
 */
public class SaveEvent<V> extends ComponentEvent<Component> {
  private static final long serialVersionUID = -6955805231803503018L;
  private V savedObject;

  public SaveEvent(Component source, boolean fromClient) {
    super(source, fromClient);
  }

  public SaveEvent(Component source, boolean fromClient, V savedObject) {
    super(source, fromClient);
    this.savedObject = savedObject;
  }

  public V getSavedObject() {
    return savedObject;
  }

  public void setSavedObject(V savedObject) {
    this.savedObject = savedObject;
  }
}
