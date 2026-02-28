<template>
	<div class="devices-view">
		<h2>设备管理</h2>

		<div class="device-actions">
			<button @click="showAddDeviceModal = true" class="add-device-btn">添加设备</button>
			<button @click="refreshDevices" class="refresh-btn">刷新设备</button>
		</div>

		<div v-if="loading" class="loading-container">
			<div class="loading-spinner">加载中...</div>
		</div>

		<div v-else-if="error" class="error-container">
			<div class="error-message">{{ error }}</div>
			<button @click="fetchDevices" class="retry-btn">重试</button>
		</div>

		<div v-else class="device-list">
			<div v-for="device in deviceList" :key="device.id" class="device-card">
				<div class="device-header">
					<h3>{{ device.deviceName }}</h3>
					<span v-if="!device.activated" class="device-status inactive">
						未激活
					</span>
					<span v-else class="device-status" :class="device.status">
						{{ device.status === 'active' ? '在线' : device.status === 'absent' ? '离线' : '维护中' }}
					</span>
				</div>
				<div class="device-info">
					<p><strong>设备ID:</strong> {{ device.deviceId }}</p>
					<p><strong>工作室:</strong> {{ device.studioCode || '-' }}</p>
					<p><strong>最后更新:</strong> {{ device.lastConnection }}</p>
					<div class="device-personnel">
						<strong>绑定人员:</strong>
						<div v-if="device.personnels" class="personnel-list">
							<template v-if="typeof device.personnels === 'object'">
								<template v-for="(personnel, key) in device.personnels">
									<span v-if="personnel.is_active" :key="key" class="personnel-tag">
										{{ personnel.user_number }}
									</span>
								</template>
							</template>
							<template v-else>
								<span v-for="userNumber in device.personnels.split(',').filter(p => p.trim())" :key="userNumber" class="personnel-tag">
									{{ userNumber }}
								</span>
							</template>
						</div>
						<span v-else>未绑定</span>
					</div>
				</div>
				<div class="device-actions">
					<button @click="connectDevice(device)" class="action-btn connect-btn">重新连接</button>
					<button @click="editDevice(device)" class="action-btn edit-btn">编辑</button>
					<button @click="deleteDevice(device.id)" class="action-btn delete-btn">删除</button>
				</div>
			</div>
		</div>

		<div v-if="!loading && deviceList.length === 0" class="empty-state">
			<p>暂无设备数据</p>
		</div>

		<div v-if="showAddDeviceModal" class="modal-overlay" @click.self="closeModal">
			<div class="modal-content">
				<div class="modal-header">
					<h3>{{ editingDeviceId ? '编辑设备' : '添加设备' }}</h3>
					<button @click="closeModal" class="close-btn">&times;</button>
				</div>
				<form @submit.prevent="submitDevice" class="device-form">
					<div class="form-group">
						<label>设备名称:</label>
						<input type="text" v-model="deviceForm.device_name" required />
					</div>
			
					
					<div class="form-section">
						<h4>人员配对 (最多3人)</h4>
						<div v-for="personnelKey in ['personnel1', 'personnel2', 'personnel3']" :key="personnelKey" class="person-pairing-item">
							<div class="person-pairing-header">
								<label>{{ personnelKey === 'personnel1' ? '人员 1' : personnelKey === 'personnel2' ? '人员 2' : '人员 3' }}</label>
								<button v-if="deviceForm.personnels[personnelKey]?.is_active" @click.prevent="removePerson(personnelKey)" class="remove-person-btn">解除</button>
							</div>
							<div class="person-pairing-content">
								<input type="text" :value="deviceForm.personnels[personnelKey]?.user_number || '未绑定人员'" 
									disabled class="person-input" />
								<button @click.prevent="openPersonModal(personnelKey)" class="pair-btn">
									{{ deviceForm.personnels[personnelKey]?.is_active ? '重新配对' : '配对' }}
								</button>
							</div>
							<div class="pairing-status" v-if="getPairingStatus(personnelKey) !== 'none'">
								<span class="status-indicator" :class="getPairingStatus(personnelKey)">
									{{ getPairingStatusText(personnelKey) }}
								</span>
							</div>
						</div>
					</div>
					
					<div class="form-actions">
						<button type="button" @click="closeModal" class="cancel-btn">取消</button>
						<button type="submit" class="save-btn" :disabled="submitting">
							{{ submitting ? '处理中...' : '保存' }}
						</button>
					</div>
				</form>
			</div>
		</div>

		<div v-if="showPersonModal" class="modal-overlay" @click.self="closePersonModal">
			<div class="modal-content">
				<div class="modal-header">
					<h3>选择人员</h3>
					<button @click="closePersonModal" class="close-btn">&times;</button>
				</div>
				<div class="person-list">
					<div v-for="person in availablePersons" :key="person.userId" class="person-item"
						@click="selectPerson(person)"
						:class="{ 'selected': selectedPerson && selectedPerson.userId === person.userId }">
						<div class="person-name">{{ person.userName }}</div>
						<div class="person-id">ID: {{ person.userId }}</div>
						<div class="person-number">编号: {{ person.userNumber }}</div>
						<div class="person-department">{{ person.workTask || '未知部门' }}</div>
					</div>
				</div>
				<div class="modal-footer">
					<button @click="closePersonModal" class="modal-cancel-btn">取消</button>
					<button @click="confirmPersonSelection" class="modal-confirm-btn" :disabled="!selectedPerson">
						确定
					</button>
				</div>
			</div>
		</div>

		<div v-if="showPairingModal" class="modal-overlay">
			<div class="modal-content">
				<div class="modal-header">
					<h3>人员配对</h3>
				</div>
				<div class="pairing-prompt">
					<div class="pairing-icon">
						<img src="../assets/配对.png" style="width: 50px; height: 50px;" alt="配对图标">
					</div>
					<h4>请{{ selectedPerson?.userName }}进行配对</h4>
					<p>请该人员站在设备前完成配对</p>
					<div class="pairing-status-text">{{ pairingStatusText }}</div>
					<div class="progress-bar">
						<div class="progress-fill" :style="{ width: pairingProgress + '%' }"></div>
					</div>
				</div>
				<div class="modal-footer">
					<button @click="cancelPairing" class="modal-cancel-btn">取消配对</button>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { useDeviceStore } from '../stores/device'
import { usePersonnelStore } from '../stores/personnel'
import mqttService from '../services/mqtt'
import webSocketService from '../services/websocket'

const deviceStore = useDeviceStore()
const personnelStore = usePersonnelStore()

const deviceList = computed(() => deviceStore.deviceList)
const loading = computed(() => deviceStore.loading)
const error = computed(() => deviceStore.error)

const showAddDeviceModal = ref(false)
const showPersonModal = ref(false)
const showPairingModal = ref(false)
const editingDeviceId = ref(null)
const submitting = ref(false)
const currentPersonIndex = ref(null)
const selectedPerson = ref(null)

const pairingStatusText = ref('正在等待配对...')
const pairingProgress = ref(0)
const pairingInterval = ref(null)
const pairingTimeout = ref(null)

const pairingStatuses = reactive(JSON.parse(localStorage.getItem('pairingStatuses') || '{}'))
const pairingComplete = ref({
	status: false,
	deviceId: '',
	message: ''
})
const isProcessingPairing = ref(false)

watch(pairingStatuses, (newStatuses) => {
	localStorage.setItem('pairingStatuses', JSON.stringify(newStatuses))
}, { deep: true })

watch(pairingComplete, async (newStatus) => {
	if (newStatus.status && !isProcessingPairing.value) {
		console.log('检测到配对完成状态变化，触发assignPerson API:', newStatus)
		isProcessingPairing.value = true
		
		try {
			await handlePairingComplete(newStatus)
		} finally {
			isProcessingPairing.value = false
			pairingComplete.value = {
				status: false,
				deviceId: '',
				message: ''
			}
		}
	}
}, { deep: true })
const deviceForm = ref({
	device_name: '',
	device_id: '',
	personnels: {
		personnel1: { is_active: false, user_number: '' },
		personnel2: { is_active: false, user_number: '' },
		personnel3: { is_active: false, user_number: '' }
	}
})

const availablePersons = computed(() => {
	const personnelList = personnelStore.personnelList || []
	return personnelList.map(person => ({
		userId: person.userId,
		userName: person.userName,
		userNumber: person.userNumber,
		workTask: person.workTask || '未知部门'
	}))
})

onMounted(async () => {
	await Promise.all([
		fetchDevices(),
		personnelStore.fetchPersonnelList()
	])
	
	await webSocketService.connect()
	
	webSocketService.on('pairing', async (message) => {
		console.log('接收到配对完成消息:', message)
		
		if (isProcessingPairing.value) {
			console.log('正在处理配对中，忽略重复消息')
			return
		}
		
		pairingComplete.value = {
			status: true,
			deviceId: message.deviceId || '',
			message: message.message || '配对成功'
		}
		console.log('已更新全局配对状态:', pairingComplete.value)
	})
})

const fetchDevices = async () => {
	await deviceStore.fetchDeviceList()
}

const closeModal = () => {
	showAddDeviceModal.value = false
	showPersonModal.value = false
	showPairingModal.value = false
	editingDeviceId.value = null
	submitting.value = false
	resetForm()
}

const resetForm = () => {
	deviceForm.value = {
		device_name: '',
		device_id: '',
		personnels: {
			personnel1: { is_active: false, user_number: '' },
			personnel2: { is_active: false, user_number: '' },
			personnel3: { is_active: false, user_number: '' }
		}
	}
}

const editDevice = (device) => {
	editingDeviceId.value = device.id
	// Convert comma-separated string to personnel object
	let personnelsObject = {
		personnel1: { is_active: false, user_number: '' },
		personnel2: { is_active: false, user_number: '' },
		personnel3: { is_active: false, user_number: '' }
	}
	
	if (device.personnels) {
		if (typeof device.personnels === 'object') {
			personnelsObject = device.personnels
		} else {
			// Convert comma-separated string to object
			const userNumbers = device.personnels.split(',').map(p => p.trim()).filter(p => p)
			userNumbers.forEach((userNumber, index) => {
				if (index < 3) {
					personnelsObject[`personnel${index + 1}`] = {
						is_active: true,
						user_number: userNumber
					}
				}
			})
		}
	}
	
	deviceForm.value = {
		device_name: device.deviceName,
		device_id: device.deviceId,
		personnels: personnelsObject
	}
	showAddDeviceModal.value = true
}

const deleteDevice = async (id) => {
	if (confirm('确定要删除该设备吗？')) {
		try {
			await deviceStore.deleteDevice(id)
		} catch (error) {
			alert('删除设备失败: ' + error.message)
		}
	}
}

const connectDevice = async (device) => {
	try {
		await deviceStore.reconnectDevice(device.id)
		alert('设备重新连接成功')
	} catch (error) {
		alert('重新连接设备失败: ' + error.message)
	}
}

const refreshDevices = async () => {
	await fetchDevices()
}

const openPersonModal = (personnelKey) => {
	currentPersonIndex.value = personnelKey
	selectedPerson.value = null
	showPersonModal.value = true
}

const closePersonModal = () => {
	showPersonModal.value = false
	selectedPerson.value = null
	currentPersonIndex.value = null
}

const selectPerson = (person) => {
	selectedPerson.value = person
}

const confirmPersonSelection = () => {
	if (!selectedPerson.value) return

	const personToPair = selectedPerson.value
	const personnelKey = currentPersonIndex.value

	deviceForm.value.personnels[personnelKey] = {
		is_active: false,
		user_number: personToPair.userNumber
	}

	closePersonModal()

	showPairingModal.value = true
	pairingStatusText.value = '正在等待配对...'
	pairingProgress.value = 0

	startPairingSimulation(personToPair, personnelKey)
}

const startPairingSimulation = async (personToPair, personnelKey) => {
	console.log('开始配对流程 - personnelKey:', personnelKey, 'person:', personToPair)

	const key = `0,${personnelKey}`
	pairingStatuses[key] = {
		status: 'pairing',
		personId: personToPair.userId
	}

	pairingProgress.value = 0
	pairingStatusText.value = '正在准备配对...'

	clearInterval(pairingInterval.value)
	clearTimeout(pairingTimeout.value)

	try {
		const deviceName = deviceForm.value.device_name
		console.log('设备名称:', deviceName)

		pairingStatusText.value = '正在开启设备广播...'
		pairingProgress.value = 20

		await mqttService.broadcastRestart(deviceName)

		pairingStatusText.value = `请 ${personToPair.userName} 前往设备处进行配对...`
		pairingProgress.value = 40

		pairingStatusText.value = '正在等待配对完成...'

		pairingInterval.value = setInterval(() => {
			if (pairingProgress.value < 80) {
				pairingProgress.value += 0.5
			}
		}, 1000)

		pairingTimeout.value = setTimeout(() => {
			clearInterval(pairingInterval.value)
			pairingStatusText.value = '配对超时，请重试'
			pairingProgress.value = 0
			pairingStatuses[key] = {
				status: 'none',
				personId: null
			}
		}, 60000)
	} catch (error) {
		console.error('配对流程失败:', error)
		pairingStatusText.value = `配对失败: ${error.message}`
		pairingProgress.value = 0
		console.log('广播失败，但仍保持配对状态，等待WebSocket消息...')
		pairingStatusText.value = '等待配对完成...(广播失败，可直接发送MQTT消息)'
	}
}

const handlePairingComplete = async (pairingData) => {
	console.log('处理配对完成:', pairingData)

	const { deviceId } = pairingData
	const deviceName = deviceId

	console.log('需要处理的设备ID:', deviceId)

	let targetPersonnelKey = null
	let userNumber = null

	for (const key in pairingStatuses) {
		if (pairingStatuses[key].status === 'pairing') {
			const [, personnelKey] = key.split(',')
			userNumber = deviceForm.value.personnels[personnelKey]?.user_number

			if (userNumber) {
				targetPersonnelKey = personnelKey
				break
			}
		}
	}

	console.log('找到的人员槽位:', targetPersonnelKey, '用户编号:', userNumber)

	if (userNumber && targetPersonnelKey) {
		const personnelNumber = targetPersonnelKey === 'personnel1' ? 1 : targetPersonnelKey === 'personnel2' ? 2 : 3

		console.log('准备调用assignPerson API:', {
			deviceName: deviceName,
			personnelNumber: personnelNumber,
			userNumber: userNumber
		})

		try {
			await mqttService.assignPerson(
				editingDeviceId.value || 0,
				deviceName,
				personnelNumber,
				{ userNumber: userNumber, name: userNumber }
			)

			const key = `0,${targetPersonnelKey}`
			pairingStatuses[key] = {
				status: 'paired',
				personId: userNumber
			}

			deviceForm.value.personnels[targetPersonnelKey].is_active = true

			pairingComplete.value = {
				status: false,
				deviceId: '',
				message: ''
			}

			showPairingModal.value = false

			clearInterval(pairingInterval.value)
			clearTimeout(pairingTimeout.value)

			alert(`设备 ${deviceName} 配对完成！`)
		} catch (error) {
			console.error('分配人员失败:', error)
			alert('配对完成但分配人员失败: ' + error.message)
		}
	} else {
		console.warn('找到设备但没有找到配对的人员信息')
		alert(`设备 ${deviceName} 配对完成，但未找到对应的人员信息。请先选择人员并开始配对。`)

		pairingComplete.value = {
			status: false,
			deviceId: '',
			message: ''
		}
	}
}

const cancelPairing = () => {
	clearInterval(pairingInterval.value)
	clearTimeout(pairingTimeout.value)
	showPairingModal.value = false

	const key = `0,${currentPersonIndex.value}`
	if (pairingStatuses[key]) {
		delete pairingStatuses[key]
	}

	currentPersonIndex.value = null
	selectedPerson.value = null
}

const removePerson = async (personnelKey) => {
	if (confirm('确定要解除该人员绑定吗？')) {
		const userNumber = deviceForm.value.personnels[personnelKey]?.user_number
		if (userNumber) {
			try {
				await mqttService.unassignPerson(editingDeviceId.value || 0, userNumber)
				console.log('解除配对成功')
			} catch (error) {
				console.error('解除配对失败:', error)
				alert('解除配对失败: ' + error.message)
			}
		}
		deviceForm.value.personnels[personnelKey] = {
			is_active: false,
			user_number: ''
		}
		const key = `0,${personnelKey}`
		if (pairingStatuses[key]) {
			delete pairingStatuses[key]
		}
	}
}

const submitDevice = async () => {
	submitting.value = true
	try {
		// Convert personnel object to comma-separated string for backend
		// Now sending personnel names instead of user numbers
		const personnelNames = []
		for (let i = 1; i <= 3; i++) {
			const personnel = deviceForm.value.personnels[`personnel${i}`]
			if (personnel && personnel.is_active && personnel.user_number) {
				// Find the personnel name by user_number
				const personInfo = availablePersons.value.find(p => p.userNumber === personnel.user_number)
				if (personInfo && personInfo.userName) {
					personnelNames.push(personInfo.userName)
				} else {
					// Fallback: use user_number if name not found
					personnelNames.push(personnel.user_number)
				}
			}
		}
		
		const backendData = {
			...deviceForm.value,
			personnels: personnelNames.join(',')
		}
		
		if (editingDeviceId.value) {
			await deviceStore.updateDevice(editingDeviceId.value, backendData)
			alert('设备更新成功')
			await refreshDevices()
		} else {
			await deviceStore.addDevice(backendData)
			alert('设备添加成功')
			await refreshDevices()
		}
		closeModal()
	} catch (error) {
		alert('操作失败: ' + error.message)
	} finally {
		submitting.value = false
	}
}

const getPairingStatus = (personnelKey) => {
	const key = `0,${personnelKey}`
	return pairingStatuses[key]?.status || 'none'
}

const getPairingStatusText = (personnelKey) => {
	const status = getPairingStatus(personnelKey)
	switch (status) {
		case 'pairing':
			return '配对中...'
		case 'paired':
			return '已配对'
		default:
			return ''
	}
}
</script>

<style scoped>
.devices-view {
	padding: 20px;
}

.device-actions {
	display: flex;
	gap: 10px;
	margin-bottom: 20px;
}

.add-device-btn,
.refresh-btn {
	padding: 10px 20px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.add-device-btn {
	background: #67c23a;
	color: white;
}

.add-device-btn:hover {
	background: #85ce61;
}

.refresh-btn {
	background: #409eff;
	color: white;
}

.refresh-btn:hover {
	background: #66b1ff;
}

.device-list {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
	gap: 20px;
}

.device-card {
	background: white;
	border-radius: 8px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
	transition: transform 0.2s;
}

.device-card:hover {
	transform: translateY(-5px);
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.device-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 15px;
	padding-bottom: 10px;
	border-bottom: 1px solid #e0e0e0;
}

.device-header h3 {
	margin: 0;
	color: #333;
	font-size: 1.1rem;
}

.device-status {
	padding: 4px 12px;
	border-radius: 12px;
	font-size: 0.8rem;
	font-weight: bold;
}

.device-status.active {
	background: #d4edda;
	color: #155724;
}

.device-status.absent {
	background: #f8d7da;
	color: #721c24;
}

.device-status.maintenance {
	background: #fff3cd;
	color: #856404;
}

.device-status.inactive {
	background: #f5f5f5;
	color: #666;
	border: 1px dashed #999;
}

.loading-container,
.error-container,
.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60px 20px;
	text-align: center;
}

.loading-spinner {
	font-size: 1.2rem;
	color: #409eff;
}

.error-message {
	color: #f56c6c;
	margin-bottom: 15px;
	font-size: 1rem;
}

.retry-btn {
	padding: 8px 16px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.retry-btn:hover {
	background: #66b1ff;
}

.empty-state {
	color: #909399;
	font-size: 1rem;
}

.device-info {
	margin-bottom: 15px;
}

.device-info p {
	margin: 8px 0;
	color: #666;
	font-size: 0.9rem;
}

.device-personnel {
	margin-top: 10px;
	padding-top: 10px;
	border-top: 1px solid #e0e0e0;
}

.personnel-list {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
	margin-top: 8px;
}

.personnel-tag {
	background: #f0f9ff;
	color: #0284c7;
	padding: 4px 10px;
	border-radius: 12px;
	font-size: 0.85rem;
}

.device-actions {
	display: flex;
	gap: 10px;
	padding-top: 15px;
	border-top: 1px solid #e0e0e0;
}

.action-btn {
	flex: 1;
	padding: 6px 12px;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.85rem;
	transition: background-color 0.2s;
}

.connect-btn {
	background: #409eff;
	color: white;
}

.connect-btn:hover {
	background: #66b1ff;
}

.edit-btn {
	background: #e6a23c;
	color: white;
}

.edit-btn:hover {
	background: #ebb563;
}

.delete-btn {
	background: #f56c6c;
	color: white;
}

.delete-btn:hover {
	background: #f78989;
}

.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 1000;
}

.modal-content {
	background: white;
	border-radius: 8px;
	width: 90%;
	max-width: 600px;
	max-height: 90vh;
	overflow-y: auto;
	box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15px 20px;
	border-bottom: 1px solid #eee;
}

.modal-header h3 {
	margin: 0;
	color: #333;
}

.close-btn {
	background: none;
	border: none;
	font-size: 1.5rem;
	color: #999;
	cursor: pointer;
	padding: 0;
	line-height: 1;
}

.close-btn:hover {
	color: #333;
}

.device-form {
	padding: 20px;
}

.form-group {
	margin-bottom: 15px;
}

.form-group label {
	display: block;
	margin-bottom: 5px;
	font-weight: bold;
	color: #333;
	font-size: 0.9rem;
}

.form-group input,
.form-group select {
	width: 100%;
	padding: 8px 12px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 0.9rem;
}

.form-section {
	margin-top: 20px;
	padding-top: 20px;
	border-top: 1px solid #e0e0e0;
}

.form-section h4 {
	margin: 0 0 15px 0;
	color: #333;
	font-size: 1rem;
}

.person-pairing-item {
	margin-bottom: 15px;
	padding: 15px;
	background: #f9f9f9;
	border-radius: 6px;
	border: 1px solid #e0e0e0;
}

.person-pairing-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 10px;
}

.person-pairing-header label {
	font-weight: bold;
	color: #333;
	font-size: 0.9rem;
}

.remove-person-btn {
	padding: 4px 10px;
	background: #f56c6c;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.8rem;
	transition: background-color 0.2s;
}

.remove-person-btn:hover {
	background: #f78989;
}

.person-pairing-content {
	display: flex;
	gap: 10px;
}

.person-input {
	flex: 1;
	padding: 8px 12px;
	border: 1px solid #ddd;
	border-radius: 4px;
	font-size: 0.9rem;
	background: #f5f5f5;
}

.pair-btn {
	padding: 8px 16px;
	background: #409eff;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.pair-btn:hover {
	background: #66b1ff;
}

.pairing-status {
	margin-top: 10px;
}

.status-indicator {
	padding: 4px 10px;
	border-radius: 12px;
	font-size: 0.8rem;
	font-weight: bold;
}

.status-indicator.pairing {
	background: #fff3cd;
	color: #856404;
}

.status-indicator.paired {
	background: #d4edda;
	color: #155724;
}

.person-list {
	max-height: 400px;
	overflow-y: auto;
}

.person-item {
	padding: 12px 15px;
	border-bottom: 1px solid #e0e0e0;
	cursor: pointer;
	transition: background-color 0.2s;
}

.person-item:hover {
	background: #f5f5f5;
}

.person-item.selected {
	background: #e6f7ff;
	border-left: 3px solid #409eff;
}

.person-name {
	font-weight: bold;
	color: #333;
	margin-bottom: 4px;
}

.person-id,
.person-number,
.person-department {
	color: #666;
	font-size: 0.85rem;
	margin: 2px 0;
}

.modal-footer {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	padding: 15px 20px;
	border-top: 1px solid #eee;
}

.modal-cancel-btn {
	padding: 8px 16px;
	background: #909399;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.modal-cancel-btn:hover {
	background: #a6a9ad;
}

.modal-confirm-btn {
	padding: 8px 16px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.modal-confirm-btn:hover {
	background: #85ce61;
}

.modal-confirm-btn:disabled {
	background: #c0c4cc;
	cursor: not-allowed;
}

.pairing-prompt {
	text-align: center;
	padding: 30px 20px;
}

.pairing-icon {
	margin-bottom: 15px;
}

.pairing-prompt h4 {
	margin: 0 0 10px 0;
	color: #333;
}

.pairing-prompt p {
	margin: 0 0 20px 0;
	color: #666;
}

.pairing-status-text {
	margin-bottom: 15px;
	color: #409eff;
	font-weight: bold;
}

.progress-bar {
	width: 100%;
	height: 8px;
	background: #e0e0e0;
	border-radius: 4px;
	overflow: hidden;
}

.progress-fill {
	height: 100%;
	background: linear-gradient(90deg, #409eff, #67c23a);
	transition: width 0.3s ease;
}

.form-actions {
	display: flex;
	justify-content: flex-end;
	gap: 10px;
	margin-top: 20px;
	padding-top: 15px;
	border-top: 1px solid #eee;
}

.cancel-btn {
	padding: 8px 16px;
	background: #909399;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.cancel-btn:hover {
	background: #a6a9ad;
}

.save-btn {
	padding: 8px 16px;
	background: #67c23a;
	color: white;
	border: none;
	border-radius: 4px;
	cursor: pointer;
	font-size: 0.9rem;
	transition: background-color 0.2s;
}

.save-btn:hover {
	background: #85ce61;
}

.save-btn:disabled {
	background: #c0c4cc;
	cursor: not-allowed;
}

@media (max-width: 768px) {
	.device-list {
		grid-template-columns: 1fr;
	}
	
	.device-actions {
		flex-direction: column;
	}
	
	.person-pairing-content {
		flex-direction: column;
	}
	
	.modal-content {
		width: 95%;
	}
}
</style>
