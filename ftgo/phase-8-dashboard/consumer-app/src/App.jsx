import React, { useState, useEffect, useRef } from 'react'
import SockJS from 'sockjs-client/dist/sockjs'
import Stomp from 'stompjs'

const API = 'http://34.93.167.128:8000'
const WS_URL = 'http://34.93.167.128:8080/ws'

const CONSUMERS = [
  { id: 1, name: 'Rahul Sharma', phone: '9876543210' },
  { id: 2, name: 'Priya Patel', phone: '9876543211' },
  { id: 3, name: 'Amit Kumar', phone: '9876543212' },
]

const STATUS_STEPS = ['PLACED', 'APPROVED', 'PREPARING', 'READY_FOR_PICKUP', 'PICKED_UP', 'DELIVERED']
const STATUS_LABELS = ['Placed', 'Approved', 'Preparing', 'Ready', 'Picked Up', 'Delivered']
const STATUS_COLORS = {
  PLACED: '#3498db', APPROVED: '#2ecc71', PREPARING: '#f39c12',
  READY_FOR_PICKUP: '#e67e22', PICKED_UP: '#9b59b6', DELIVERED: '#27ae60',
  CANCELLED: '#e74c3c', PENDING_ACCEPTANCE: '#95a5a6'
}

export default function App() {
  const [tab, setTab] = useState('restaurants')
  const [consumer, setConsumer] = useState(CONSUMERS[0])
  const [restaurants, setRestaurants] = useState([])
  const [orders, setOrders] = useState([])
  const [events, setEvents] = useState([])
  const [connected, setConnected] = useState(false)
  const [placing, setPlacing] = useState(false)
  const stompRef = useRef(null)
  const ordersRef = useRef(orders)
  ordersRef.current = orders

  // Fetch restaurants
  useEffect(() => {
    fetch(API + '/api/restaurants/active')
      .then(r => r.json())
      .then(data => Array.isArray(data) ? setRestaurants(data) : setRestaurants([]))
      .catch(() => setRestaurants([]))
  }, [])

  // Fetch orders
  const fetchOrders = () => {
    fetch(API + '/api/orders/consumer/' + consumer.id)
      .then(r => r.json())
      .then(data => Array.isArray(data) ? setOrders(data) : setOrders([]))
      .catch(() => setOrders([]))
  }
  useEffect(() => { fetchOrders() }, [consumer, tab])

  // WebSocket
  useEffect(() => {
    const socket = new SockJS(WS_URL)
    const client = Stomp.over(socket)
    client.debug = null
    client.connect({}, () => {
      setConnected(true)
      client.subscribe('/topic/order-updates', msg => {
        const evt = JSON.parse(msg.body)
        setEvents(prev => [evt, ...prev.slice(0, 49)])
        // Auto-refresh orders on any update
        setTimeout(fetchOrders, 500)
      })
    }, () => {
      setConnected(false)
      setTimeout(() => window.location.reload(), 5000)
    })
    stompRef.current = client
    return () => { try { client.disconnect() } catch (e) { } }
  }, [])

  const placeOrder = async (restaurantId, restaurantName, menuItemId, menuItemName, price) => {
    setPlacing(true)
    try {
      const res = await fetch(API + '/api/orders', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          consumerId: consumer.id,
          restaurantId,
          menuItemIds: [menuItemId],
          quantities: [1],
          deliveryAddress: consumer.name + "'s address"
        })
      })
      if (res.ok) {
        setTab('orders')
        setTimeout(fetchOrders, 2000)
      }
    } catch (e) { console.error(e) }
    setPlacing(false)
  }

  return (
    <div style={styles.app}>
      {/* Status bar */}
      <div style={styles.statusBar}>
        <span>FTGO</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
          <div style={{ ...styles.wsDot, background: connected ? '#2ecc71' : '#e74c3c' }} />
          <span style={{ fontSize: 11 }}>{connected ? 'Live' : 'Offline'}</span>
        </div>
      </div>

      {/* Header */}
      <div style={styles.header}>
        <div>
          <div style={{ fontSize: 20, fontWeight: 700 }}>Hello, {consumer.name.split(' ')[0]} 👋</div>
          <div style={{ fontSize: 13, color: '#999', marginTop: 2 }}>What would you like to eat?</div>
        </div>
        <select style={styles.userSelect} value={consumer.id} onChange={e => setConsumer(CONSUMERS.find(c => c.id === +e.target.value))}>
          {CONSUMERS.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </div>

      {/* Content */}
      <div style={styles.content}>
        {tab === 'restaurants' && (
          <div>
            <h2 style={styles.sectionTitle}>Restaurants</h2>
            {restaurants.map(r => (
              <div key={r.id} style={styles.restaurantCard}>
                <div style={styles.restaurantHeader}>
                  <div style={{ fontSize: 17, fontWeight: 600 }}>{r.name}</div>
                  <div style={{ fontSize: 12, color: '#999' }}>{r.address}</div>
                </div>
                <div style={styles.menuList}>
                  {(r.menuItems || []).filter(m => m.available).map(m => (
                    <div key={m.id} style={styles.menuItem}>
                      <div>
                        <div style={{ fontWeight: 500 }}>{m.name}</div>
                        <div style={{ color: '#27ae60', fontWeight: 600, fontSize: 14 }}>₹{m.price}</div>
                      </div>
                      <button style={styles.addBtn} disabled={placing}
                        onClick={() => placeOrder(r.id, r.name, m.id, m.name, m.price)}>
                        {placing ? '...' : 'Order'}
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}

        {tab === 'orders' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h2 style={styles.sectionTitle}>My Orders</h2>
              <button onClick={fetchOrders} style={styles.refreshBtn}>↻ Refresh</button>
            </div>
            {orders.length === 0 && <div style={styles.empty}>No orders yet. Browse restaurants to place one!</div>}
            {orders.map(o => (
              <div key={o.orderId} style={{ ...styles.orderCard, borderLeftColor: STATUS_COLORS[o.status] || '#555' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                  <div>
                    <div style={{ fontWeight: 600 }}>Order #{o.orderId}</div>
                    <div style={{ fontSize: 13, color: '#999' }}>{o.restaurantName}</div>
                  </div>
                  <div style={{ ...styles.statusBadge, background: STATUS_COLORS[o.status] || '#555' }}>
                    {o.status}
                  </div>
                </div>
                <div style={{ fontSize: 13, color: '#aaa', marginBottom: 8 }}>
                  ₹{o.totalAmount} • {o.deliveryAddress}
                  {o.paymentStatus && <span> • Payment: {o.paymentStatus}</span>}
                </div>
                {/* Progress bar */}
                <div style={styles.progressBar}>
                  {STATUS_STEPS.map((s, i) => {
                    const currentIdx = STATUS_STEPS.indexOf(o.status)
                    const done = i <= currentIdx
                    const current = i === currentIdx
                    return (
                      <React.Fragment key={s}>
                        <div style={{
                          ...styles.progressDot,
                          background: done ? '#27ae60' : (current ? '#3498db' : '#333'),
                          animation: current ? 'pulse 1.5s infinite' : 'none'
                        }}>
                          {done ? '✓' : i + 1}
                        </div>
                        {i < STATUS_STEPS.length - 1 && (
                          <div style={{ ...styles.progressLine, background: i < currentIdx ? '#27ae60' : '#333' }} />
                        )}
                      </React.Fragment>
                    )
                  })}
                </div>
                <div style={styles.progressLabels}>
                  {STATUS_LABELS.map(l => <span key={l} style={{ fontSize: 9 }}>{l}</span>)}
                </div>
              </div>
            ))}
          </div>
        )}

        {tab === 'live' && (
          <div>
            <h2 style={styles.sectionTitle}>Live Events</h2>
            {events.length === 0 && <div style={styles.empty}>Waiting for events... Place an order!</div>}
            {events.map((e, i) => (
              <div key={i} style={{ ...styles.eventItem, borderLeftColor: e.source === 'kitchen' ? '#f39c12' : e.source === 'delivery' ? '#9b59b6' : e.source === 'accounting' ? '#e74c3c' : '#3498db' }}>
                <div style={{ fontWeight: 600, fontSize: 13 }}>{e.status}</div>
                <div style={{ fontSize: 11, color: '#888' }}>Order #{e.orderId} • {e.source} • {e.time}</div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Bottom nav */}
      <div style={styles.bottomNav}>
        {[
          { id: 'restaurants', icon: '🍽️', label: 'Browse' },
          { id: 'orders', icon: '📦', label: 'Orders' },
          { id: 'live', icon: '⚡', label: 'Live' },
        ].map(t => (
          <div key={t.id} style={{ ...styles.navItem, color: tab === t.id ? '#f39c12' : '#666' }}
            onClick={() => setTab(t.id)}>
            <div style={{ fontSize: 22 }}>{t.icon}</div>
            <div style={{ fontSize: 10, fontWeight: 600 }}>{t.label}</div>
          </div>
        ))}
      </div>

      <style>{`@keyframes pulse { 0%,100%{transform:scale(1)} 50%{transform:scale(1.2)} }`}</style>
    </div>
  )
}

const styles = {
  app: { maxWidth: 430, margin: '0 auto', background: '#0a0a1a', minHeight: '100vh', fontFamily: "'Inter', sans-serif", color: '#e0e0e0', position: 'relative', paddingBottom: 70 },
  statusBar: { background: '#111', padding: '8px 16px', fontSize: 12, display: 'flex', justifyContent: 'space-between', alignItems: 'center', color: '#888' },
  wsDot: { width: 8, height: 8, borderRadius: '50%' },
  header: { padding: '20px 16px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  userSelect: { background: '#1a1a2e', border: '1px solid #333', borderRadius: 8, padding: '8px 10px', color: '#e0e0e0', fontSize: 12 },
  content: { padding: '0 16px', overflow: 'auto' },
  sectionTitle: { fontSize: 18, fontWeight: 700, marginBottom: 16, color: '#fff' },
  restaurantCard: { background: '#12122a', borderRadius: 14, marginBottom: 16, overflow: 'hidden', border: '1px solid #1e1e3a' },
  restaurantHeader: { padding: '16px 16px 12px', background: 'linear-gradient(135deg, #1a1a35, #12122a)' },
  menuList: { padding: '0 16px 12px' },
  menuItem: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px 0', borderBottom: '1px solid #1e1e3a' },
  addBtn: { background: '#27ae60', border: 'none', color: 'white', padding: '8px 18px', borderRadius: 20, fontWeight: 600, fontSize: 13, cursor: 'pointer' },
  orderCard: { background: '#12122a', borderRadius: 14, padding: 16, marginBottom: 12, borderLeft: '4px solid #555', border: '1px solid #1e1e3a' },
  statusBadge: { padding: '4px 10px', borderRadius: 12, fontSize: 11, fontWeight: 600, color: 'white' },
  progressBar: { display: 'flex', alignItems: 'center', gap: 0, marginTop: 4 },
  progressDot: { width: 22, height: 22, borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 10, fontWeight: 700, color: 'white', flexShrink: 0 },
  progressLine: { height: 2, flex: 1 },
  progressLabels: { display: 'flex', justifyContent: 'space-between', marginTop: 4, color: '#666' },
  eventItem: { background: '#12122a', borderRadius: 8, padding: '10px 14px', marginBottom: 8, borderLeft: '3px solid #555' },
  empty: { textAlign: 'center', padding: '40px 20px', color: '#555', fontSize: 14 },
  refreshBtn: { background: '#1a1a35', border: '1px solid #333', color: '#aaa', padding: '6px 14px', borderRadius: 6, cursor: 'pointer', fontSize: 13 },
  bottomNav: { position: 'fixed', bottom: 0, left: '50%', transform: 'translateX(-50%)', width: '100%', maxWidth: 430, background: '#111', borderTop: '1px solid #222', display: 'flex', justifyContent: 'space-around', padding: '8px 0 12px' },
  navItem: { textAlign: 'center', cursor: 'pointer', padding: '4px 16px' },
}
