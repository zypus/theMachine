package com.the.machine.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.the.machine.framework.interfaces.Observable;
import com.the.machine.framework.interfaces.Observer;
import lombok.Getter;
import lombok.Setter;

import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * TODO Add description
 *
 * @author Fabian Fraenz <f.fraenz@t-online.de>
 * @created 19/02/15
 */
public abstract class ObservableComponent
		extends Component implements Observable {
	transient @Getter @Setter protected WeakReference<Entity>
	owner=null;

	transient private boolean          changed = false;
	transient private Vector<Observer> obs     = new Vector<>();

	/**
	 * Adds an observer to the set of observers for this object, provided
	 * that it is not the same as some observer already in the set.
	 * The order in which notifications will be delivered to multiple
	 * observers is not specified. See the class comment.
	 *
	 * @param o
	 * 		an observer to be added.
	 *
	 * @throws NullPointerException
	 * 		if the parameter o is null.
	 */
	public synchronized void addObserver(Observer o) {
		if (o == null)
			throw new NullPointerException();
		if (!obs.contains(o)) {
			obs.addElement(o);
		}
	}

	/**
	 * Deletes an observer from the set of observers of this object.
	 * Passing <CODE>null</CODE> to this method will have no effect.
	 *
	 * @param o
	 * 		the observer to be deleted.
	 */
	public synchronized void deleteObserver(Observer o) {
		obs.removeElement(o);
	}

	/**
	 * If this object has changed, as indicated by the
	 * <code>hasChanged</code> method, then notify all of its observers
	 * and then call the <code>clearChanged</code> method to
	 * indicate that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two
	 * arguments: this observable object and <code>null</code>. In other
	 * words, this method is equivalent to:
	 * <blockquote><tt>
	 * notifyObservers(null)</tt></blockquote>
	 *
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void notifyObservers() {
		notifyObservers(null);
	}

	/**
	 * If this object has changed, as indicated by the
	 * <code>hasChanged</code> method, then notify all of its observers
	 * and then call the <code>clearChanged</code> method to indicate
	 * that this object has no longer changed.
	 * <p>
	 * Each observer has its <code>update</code> method called with two
	 * arguments: this observable object and the <code>arg</code> argument.
	 *
	 * @param arg
	 * 		any object.
	 *
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#hasChanged()
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void notifyObservers(Object arg) {
        /*
         * a temporary array buffer, used as a snapshot of the state of
         * current Observers.
         */
		Object[] arrLocal;

		synchronized (this) {
            /* We don't want the Observer doing callbacks into
             * arbitrary code while holding its own Monitor.
             * The code where we extract each Observable from
             * the Vector and store the state of the Observer
             * needs synchronization, but notifying observers
             * does not (should not).  The worst result of any
             * potential race-condition here is that:
             * 1) a newly-added Observer will miss a
             *   notification in progress
             * 2) a recently unregistered Observer will be
             *   wrongly notified when it doesn't care
             */
			if (!changed)
				return;
			arrLocal = obs.toArray();
			clearChanged();
		}

		for (int i = arrLocal.length-1; i>=0; i--)
			((Observer)arrLocal[i]).update(this, arg);
	}

	/**
	 * Clears the observer list so that this object no longer has any observers.
	 */
	public synchronized void deleteObservers() {
		obs.removeAllElements();
	}

	@Override
	public void forceChanged() {
		changed = true;
	}

	/**
	 * Marks this <tt>Observable</tt> object as having been changed; the
	 * <tt>hasChanged</tt> method will now return <tt>true</tt>.
	 */
	protected synchronized void setChanged() {
		changed = true;
	}

	/**
	 * Indicates that this object has no longer changed, or that it has
	 * already notified all of its observers of its most recent change,
	 * so that the <tt>hasChanged</tt> method will now return <tt>false</tt>.
	 * This method is called automatically by the
	 * <code>notifyObservers</code> methods.
	 *
	 * @see java.util.Observable#notifyObservers()
	 * @see java.util.Observable#notifyObservers(java.lang.Object)
	 */
	protected synchronized void clearChanged() {
		changed = false;
	}

	/**
	 * Tests if this object has changed.
	 *
	 * @return <code>true</code> if and only if the <code>setChanged</code>
	 * method has been called more recently than the
	 * <code>clearChanged</code> method on this object;
	 * <code>false</code> otherwise.
	 *
	 * @see java.util.Observable#clearChanged()
	 * @see java.util.Observable#setChanged()
	 */
	public synchronized boolean hasChanged() {
		return changed;
	}

	/**
	 * Returns the number of observers of this <tt>Observable</tt> object.
	 *
	 * @return the number of observers of this object.
	 */
	public synchronized int countObservers() {
		return obs.size();
	}
}
