import {
    auth,
    googleProvider,
    firestore
} from "./firebase-init.js";
import {
    signInWithPopup,
    onAuthStateChanged,
    signOut as firebaseSignOut,
    sendSignInLinkToEmail,
    signInWithEmailLink,
    isSignInWithEmailLink
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-auth.js";
import {
    doc,
    setDoc,
    getDoc,
    deleteDoc,
    collection,
    onSnapshot,
    writeBatch,
    addDoc,
    serverTimestamp,
    query,
    where,
    Timestamp
} from "https://www.gstatic.com/firebasejs/11.10.0/firebase-firestore.js";

const projectListeners = new Map();
let listenerCounter = 0;

const settingsListeners = new Map();
let settingsListenerCounter = 0;

const kittyFeedListeners = new Map();
let kittyFeedListenerCounter = 0;

const timerListeners = new Map();
let timerListenerCounter = 0;

const teamListeners = new Map();
let teamListenerCounter = 0;

function getUserId() {
    const user = auth.currentUser;
    if (!user) {
        throw new Error("[Firebase] No authenticated user.");
    }
    return user.uid;
}

function randomTimestampSinceLastMonth() {
    const now = new Date();
    const startOfLastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const start = startOfLastMonth.getTime();
    const end = now.getTime();
    const randomTime = Math.floor(Math.random() * (end - start)) + start;
    return new Date(randomTime);
}

function getTimerDocRef(userId) {
    const uid = userId || getUserId();
    return doc(firestore, "users", uid, "timer", "timer");
}

function getKittyFeedDatesCollectionRef() {
    const userId = getUserId();
    const bowlProgressDocRef = doc(firestore, "users", userId, "game", "bowlProgress");
    return collection(bowlProgressDocRef, "feedingDates");
}

// ===== Exercise Progress =====
const exerciseListeners = new Map();
let exerciseListenerCounter = 0;

function getExerciseDocRef(userId) {
  const uid = userId || getUserId();
  return doc(firestore, "users", uid, "exercise", "progress");
}

window.myAppJsFirebase = {
    signInWithGooglePopup: async () => {
        try {
            const result = await signInWithPopup(auth, googleProvider);
            return {
                success: true,
                uid: result.user.uid,
                email: result.user.email
            };
        } catch (error) {
            let msg = error.message || "Unknown error";
            if (error.code === 'auth/popup-closed-by-user' || error.code === 'auth/cancelled-popup-request') {
                msg = 'Popup closed or cancelled.';
            }
            return {
                success: false,
                error: msg
            };
        }
    },

    signOut: async () => {
        try {
            await firebaseSignOut(auth);
            return {
                success: true
            };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    observeAuthState: (kotlinCallback) => {
        return onAuthStateChanged(auth, (user) => {
            kotlinCallback(!!user, user?.uid, user?.email, user?.displayName);
        });
    },

    createUser: async function(id, email, name) {
        try {
            const userRef = doc(firestore, "users", id);
            const docSnap = await getDoc(userRef);

            const data = { email };
            if (name !== null) {
                data.name = name;
            }

            if (docSnap.exists()) {
                await setDoc(userRef, data, { merge: true });
            } else {
                data.createdAt = serverTimestamp();
                await setDoc(userRef, data);
            }

            return { success: true };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    listenToProjects: (kotlinCallback) => {
        const listenerId = `listener_${listenerCounter++}`;
        const projectsCollection = collection(firestore, "users", getUserId(), "projects");
        const unsubscribe = onSnapshot(projectsCollection, (snapshot) => {
            const projects = [];
            snapshot.forEach(doc => {
                projects.push({
                    id: doc.id,
                    ...doc.data()
                });
            });
            kotlinCallback(JSON.stringify(projects));
        }, (error) => {
            console.error("[Firebase] Projects snapshot error:", error);
        });

        projectListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeProjectListener: (listenerId) => {
        const unsubscribe = projectListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            projectListeners.delete(listenerId);
        }
    },

    saveProject: async (projectJson) => {
        try {
            const project = JSON.parse(projectJson);
            const projectRef = doc(firestore, "users", getUserId(), "projects", project.id);
            await setDoc(projectRef, project);
            return {
                success: true
            };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    removeProjectById: async (projectId) => {
        try {
            const projectRef = doc(firestore, "users", getUserId(), "projects", projectId);
            await deleteDoc(projectRef);
            return {
                success: true
            };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    getProjectById: async (projectId) => {
        try {
            const projectRef = doc(firestore, "users", getUserId(), "projects", projectId);
            const docSnap = await getDoc(projectRef);

            if (docSnap.exists()) {
                return {
                    success: true,
                    projectJson: JSON.stringify({
                        id: docSnap.id,
                        ...docSnap.data()
                    })
                };
            } else {
                return {
                    success: false,
                    error: "Project not found."
                };
            }
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    updateProjects: async (projectsJson) => {
        try {
            const projects = JSON.parse(projectsJson);
            const batch = writeBatch(firestore);
            const projectsCollectionRef = collection(firestore, "users", getUserId(), "projects");

            projects.forEach((project) => {
                if (!project.id) {
                    throw new Error("Project missing id");
                }
                const projectDocRef = doc(projectsCollectionRef, project.id);
                batch.set(projectDocRef, project);
            });

            await batch.commit();
            return {
                success: true
            };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    getTimeSettings: (kotlinCallback) => {
        const listenerId = `settingsListener_${settingsListenerCounter++}`;
        const docRef = doc(firestore, "users", getUserId(), "settings", "timeSettings");

        const unsubscribe = onSnapshot(docRef, (docSnap) => {
            if (docSnap.exists()) {
                const json = JSON.stringify(docSnap.data());
                kotlinCallback(json);
            } else {
                kotlinCallback(null);
            }
        }, () => {
            kotlinCallback(null);
        });

        settingsListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeTimeSettingsListener: (listenerId) => {
        const unsubscribe = settingsListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            settingsListeners.delete(listenerId);
        }
    },

    setTimeSettings: async (timeSettingsJson) => {
        try {
            const data = JSON.parse(timeSettingsJson);
            const docRef = doc(firestore, "users", getUserId(), "settings", "timeSettings");
            await setDoc(docRef, data);
            return {
                success: true
            };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    getAlarmSound: (kotlinCallback) => {
        const listenerId = `alarmSoundListener_${settingsListenerCounter++}`;
        const docRef = doc(firestore, "users", getUserId(), "settings", "alarmSound");

        const unsubscribe = onSnapshot(docRef, (docSnap) => {
            if (docSnap.exists()) {
                const data = docSnap.data();
                const soundName = data?.sound || "STANDARD";
                kotlinCallback(soundName);
            } else {
                kotlinCallback("STANDARD");
            }
        }, () => {
            kotlinCallback("STANDARD");
        });

        settingsListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeAlarmSoundListener: (listenerId) => {
        const unsubscribe = settingsListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            settingsListeners.delete(listenerId);
        }
    },

    setAlarmSound: async (soundName) => {
        try {
            const data = {
                sound: soundName
            };
            const docRef = doc(firestore, "users", getUserId(), "settings", "alarmSound");
            await setDoc(docRef, data);
            return {
                success: true
            };
        } catch (error) {
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    // --- Game Progress (Bowl Filling) ---
    listenToBowlProgress: (kotlinCallback) => {
        try {
            const userId = getUserId(); // Ensure user is authenticated
            const progressDocRef = doc(firestore, "users", userId, "game", "bowlProgress");
            const listenerId = `bowlProgressListener_${listenerCounter++}`; // Use shared listenerCounter or a new one

            const unsubscribe = onSnapshot(progressDocRef, (docSnap) => {
                if (docSnap.exists()) {
                    const data = docSnap.data();
                    const progress = data.current !== undefined ? data.current : 0; // Default to 0 if 'current' field is missing
                    kotlinCallback(progress); // Send the integer directly
                } else {

                    kotlinCallback(0);
                }
            }, (error) => {
                console.error("[Firebase] Bowl progress snapshot error:", error);
                kotlinCallback(0); // Send 0 on error
            });

            projectListeners.set(listenerId, unsubscribe); // You can reuse projectListeners or create a gameListeners map
            return listenerId;
        } catch (error) {
            console.error("[Firebase] Error setting up bowl progress listener:", error.message)
            throw error; // Rethrow to signal failure to Kotlin if getUserId fails
        }
    },

    removeBowlProgressListener: (listenerId) => {
        const unsubscribe = projectListeners.get(listenerId); // Use the same map as projects or a new one
        if (unsubscribe) {
            unsubscribe();
            projectListeners.delete(listenerId);
        }
    },

    setBowlProgress: async (progressValue) => {
        try {
            const userId = getUserId();
            const progressDocRef = doc(firestore, "users", userId, "game", "bowlProgress");
            // We are setting a document where one field 'current' holds the progress
            await setDoc(progressDocRef, {
                current: progressValue
            }, {
                merge: true
            }); // merge: true is good practice
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] Error setting bowl progress:", error.message);
            return {
                success: false,
                error: error.message || "Unknown error setting bowl progress"
            };
        }
    },

    resetBowlProgress: async () => { // Corresponds to resetProgress in Kotlin
        try {
            const userId = getUserId();
            const progressDocRef = doc(firestore, "users", userId, "game", "bowlProgress");
            await setDoc(progressDocRef, {
                current: 0
            }, {
                merge: true
            }); // Reset to 0
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] Error resetting bowl progress:", error.message);
            return {
                success: false,
                error: error.message || "Unknown error resetting bowl progress"
            };
        }
    },
    listenToKittyFeedDates: (kotlinCallback) => {
        try {
            const kittyFeedDatesCollectionRef = getKittyFeedDatesCollectionRef();
            const listenerId = `kittyFeedDatesListener_${listenerCounter++}`;

            const unsubscribe = onSnapshot(kittyFeedDatesCollectionRef, (snapshot) => {
                const dates = [];
                snapshot.forEach(docSnap => {
                    const data = docSnap.data();
                    if (data.timestamp) {
                        dates.push(data.timestamp);
                    }
                });
                kotlinCallback(JSON.stringify(dates));
            }, (error) => {
                console.error("[Firebase] Error setting up kittyFeedDates listener:", error);
                kotlinCallback("[]"); // Отправляем пустой список при ошибке
            });

            projectListeners.set(listenerId, unsubscribe);
            return listenerId;
        } catch (error) {
            console.error("[Firebase] Error setting up kittyFeedDates listener:", error);
            throw error;
        }
    },

    removeKittyFeedDatesListener: (listenerId) => {
        const unsubscribe = projectListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            projectListeners.delete(listenerId);
        }
    },

    addKittyFeedDate: async (date) => {
        try {
            const userId = getUserId();
            const kittyFeedDatesCollectionRef = collection(firestore, "users", userId, "game", "bowlProgress", "feedingDates");
            const millis = Number(date); // гарантируем, что это JS Number, а не BigInt
            await addDoc(kittyFeedDatesCollectionRef, {
                timestamp: millis
            });
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] Error adding kittyFeedDate:", error);
            return {
                success: false,
                error: error.message || "Unknown error adding kittyFeedDate"
            };
        }
    },

    listenToLastResetTime: (kotlinCallback) => {
        try {
            const userId = getUserId(); // Получаем текущего пользователя
            const docRef = doc(firestore, "users", userId, "game", "resetTime");
            const listenerId = `lastResetTimeListener_${listenerCounter++}`;

            const unsubscribe = onSnapshot(docRef, (docSnap) => {
                if (docSnap.exists()) {
                    const data = docSnap.data();
                    const current = data?.current ?? 0;
                    kotlinCallback(current);
                } else {
                    kotlinCallback(0);
                }
            }, (error) => {
                console.error("[Firebase] Error listening to last reset time:", error);
                kotlinCallback(0);
            });

            projectListeners.set(listenerId, unsubscribe); // Можно использовать тот же map, что и для проектов
            return listenerId;
        } catch (error) {
            console.error("[Firebase] Failed to set up last reset time listener:", error);
            throw error;
        }
    },

    removeLastResetTimeListener: (listenerId) => {
        const unsubscribe = projectListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            projectListeners.delete(listenerId);
        }
    },

    updateLastResetTime: async (now) => {
        try {
            const userId = getUserId();
            const docRef = doc(firestore, "users", userId, "game", "resetTime");
            await setDoc(docRef, {
                current: now
            }, {
                merge: true
            });
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] Error updating last reset time:", error);
            return {
                success: false,
                error: error.message || "Unknown error updating last reset time"
            };
        }
    },

    listenToFishNumber: (kotlinCallback) => {
        try {
            const userId = getUserId(); // Ensure user is authenticated
            const progressDocRef = doc(firestore, "users", userId, "game", "fishNumber");
            const listenerId = `bowlProgressListener_${listenerCounter++}`; // Use shared listenerCounter or a new one

            const unsubscribe = onSnapshot(progressDocRef, (docSnap) => {
                if (docSnap.exists()) {
                    const data = docSnap.data();
                    const progress = data.current !== undefined ? data.current : 0; // Default to 0 if 'current' field is missing
                    kotlinCallback(progress); // Send the integer directly
                } else {

                    kotlinCallback(0);
                }
            }, (error) => {
                console.error("[Firebase] Fish Number snapshot error:", error);
                kotlinCallback(0); // Send 0 on error
            });

            projectListeners.set(listenerId, unsubscribe); // You can reuse projectListeners or create a gameListeners map
            return listenerId;
        } catch (error) {
            console.error("[Firebase] Error setting up bowl progress listener:", error.message)
            throw error; // Rethrow to signal failure to Kotlin if getUserId fails
        }
    },

    removeFishNumberProgressListener: (listenerId) => {
        const unsubscribe = projectListeners.get(listenerId); // Use the same map as projects or a new one
        if (unsubscribe) {
            unsubscribe();
            projectListeners.delete(listenerId);
        }
    },

    setFishNumber: async (progressValue) => {
        try {
            const userId = getUserId();
            const progressDocRef = doc(firestore, "users", userId, "game", "fishNumber");
            // We are setting a document where one field 'current' holds the progress
            await setDoc(progressDocRef, {
                current: progressValue
            }, {
                merge: true
            }); // merge: true is good practice
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] Error setting bowl progress:", error.message);
            return {
                success: false,
                error: error.message || "Unknown error setting bowl progress"
            };
        }
    },


    listenToTimer: (kotlinCallback) => {
        const listenerId = `timerListener_${timerListenerCounter++}`;
        const docRef = getTimerDocRef();

        const unsubscribe = onSnapshot(docRef, (docSnap) => {
            if (docSnap.exists()) {
                const data = docSnap.data();

                if (data.startAt instanceof Timestamp) {
                    data.startAt = data.startAt.toMillis();
                }

                kotlinCallback(JSON.stringify(data));
            } else {
                kotlinCallback(null);
            }
        }, () => {
            kotlinCallback(null);
        });

        timerListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeTimerListener: (listenerId) => {
        const unsubscribe = timerListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            timerListeners.delete(listenerId);
        }
    },

    listenToTimerByUserId: (userId, kotlinCallback) => {
        const listenerId = `timerListener_${timerListenerCounter++}`;
        const docRef = getTimerDocRef(userId);

        const unsubscribe = onSnapshot(docRef, (docSnap) => {
            if (docSnap.exists()) {
             const data = docSnap.data();
                if (data.startAt instanceof Timestamp) {
                      data.startAt = data.startAt.toMillis();
                }

                const json = JSON.stringify(data);
                kotlinCallback(json);
            } else {
                kotlinCallback(null);
            }
        }, () => kotlinCallback(null));

        timerListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeTimerByUserIdListener: (listenerId) => {
        const unsubscribe = timerListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            timerListeners.delete(listenerId);
        }
    },

    setTimer: async (timerJson) => {
        try {
            const docRef = getTimerDocRef();
            const data = JSON.parse(timerJson);

             if (data.mode && data.mode !== "NONE" && (data.startAt === null || data.startAt === undefined)) {
                data.startAt = serverTimestamp();
             }

            console.log("[setTimer payload]", data);
            await setDoc(docRef, data, {
                merge: false
            });
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase Timer] setTimer error:", error);
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    sendFeedback: async (stars, message) => {
        try {
            const user = auth.currentUser;
            if (!user) {
                throw new Error("User not authenticated");
            }

            const emailDoc = {
                to: ["tomato.paws4@gmail.com", "bigfatmonkey1992@gmail.com"],
                message: {
                    subject: `New feedback from ${user.email || "Anonymous"}`,
                    text: `Stabeautifulrs: ${stars}\nMessage: ${message}`,
                    html: `<p>⭐ ${stars}</p><p>${message}</p>`
                },
                stars: stars,
                userId: user.uid,
                email: user.email,
                createdAt: serverTimestamp()
            };

            const mailCollection = collection(firestore, "mail");
            await addDoc(mailCollection, emailDoc);

            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] sendFeedback error:", error);
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },
    saveEvent: async (eventJson) => {
        try {
            const event = JSON.parse(eventJson);
            const userId = getUserId();
            const eventsCollectionRef = collection(firestore, "events");

            const docData = {
                userId: userId,
                eventType: event.eventType,
                meta: event.meta,
                timeZone: event.timeZone,
                timestamp: serverTimestamp()
            };

            await addDoc(eventsCollectionRef, docData);
            return {
                success: true
            };
        } catch (error) {
            console.error("[Firebase] saveEvent error:", error);
            return {
                success: false,
                error: error.message || "Unknown error saving event"
            };
        }
    },

    sendLoginLink: async (email) => {
        try {
            const actionCodeSettings = {
                url: window.location.origin,
                handleCodeInApp: true
            };
            await sendSignInLinkToEmail(auth, email, actionCodeSettings);
            window.localStorage.setItem("emailForSignIn", email);
            return {
                success: true
            };
        } catch (error) {
            console.log("[Firebase] sendLoginLink error:", error);
            return {
                success: false,
                error: error.message || "Unknown error"
            };
        }
    },

    completeLoginWithLink: async (url) => {
        try {
            if (isSignInWithEmailLink(auth, url)) {
                const email = window.localStorage.getItem("emailForSignIn");
                if (!email) {
                    console.log("No email stored for sign-in");
                }
                const result = await signInWithEmailLink(auth, email, url);
                window.localStorage.removeItem("emailForSignIn");
                return {
                    success: true
                };
            } else {
                throw new Error("Invalid sign-in link");
            }
        } catch (error) {
            let msg = error.message || "Unknown error";
            if (error.code === "auth/invalid-action-code" || error.code === "auth/expired-action-code") {
                msg = "Invalid or expired link. Please request a new one.";
            }
            return {
                success: false,
                error: msg
            };
        } finally {
            // clear url
            const cleanUrl = window.location.origin + window.location.pathname;
            window.history.replaceState(null, "", cleanUrl);
        }
    },

    // --- team: мой teamId (users/{uid}.teamId) ---
    listenToMyTeamId: (kotlinCallback) => {
        const listenerId = `team_my_${teamListenerCounter++}`;
        const userRef = doc(firestore, "users", getUserId());

        const unsubscribe = onSnapshot(userRef, (snap) => {
            if (!snap.exists()) {
                kotlinCallback(null);
                return;
            }
            const data = snap.data();
            const teamId = (data && Object.prototype.hasOwnProperty.call(data, "teamId")) ? data.teamId : null;
            kotlinCallback(teamId ?? null);
        }, (error) => {
            console.error("[Firebase] listenToMyTeamId error:", error);
            kotlinCallback(null);
        });

        teamListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeMyTeamIdListener: (listenerId) => {
        const unsubscribe = teamListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            teamListeners.delete(listenerId);
        }
    },

    // --- team: участники по teamId (users where teamId == ...) ---
    listenToTeamMembers: (teamId, kotlinCallback) => {
        const listenerId = `team_members_${teamListenerCounter++}`;
        const usersCol = collection(firestore, "users");
        const q = query(usersCol, where("teamId", "==", teamId));

        const unsubscribe = onSnapshot(q, (snapshot) => {
            const members = [];
            snapshot.forEach((docSnap) => {
                const data = docSnap.data() || {};
                members.push({
                    id: docSnap.id, // ← doc id
                    email: String(data.email ?? ""), // ← всегда строка
                    name: Object.prototype.hasOwnProperty.call(data, "name") ?
                        data.name // ← может быть ""
                        :
                        null // ← или null, если поля нет
                });
            });
            kotlinCallback(JSON.stringify(members));
        }, (error) => {
            console.error("[Firebase] listenToTeamMembers error:", error);
            kotlinCallback("[]");
        });

        teamListeners.set(listenerId, unsubscribe);
        return listenerId;
    },

    removeTeamMembersListener: (listenerId) => {
        const unsubscribe = teamListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            teamListeners.delete(listenerId);
        }
    },

        listenToExerciseProgress: (kotlinCallback) => {
            const listenerId = `exercise_${exerciseListenerCounter++}`;
            const ref = getExerciseDocRef();

            const unsubscribe = onSnapshot(ref, (docSnap) => {
                if (docSnap.exists()) {
                    const data = docSnap.data();

                    if (data.lastExercisesUpdateTime instanceof Timestamp) {
                        data.lastExercisesUpdateTime = data.lastExercisesUpdateTime.toMillis();
                    }

                    if (data.lastExercisesUpdateTime === null || data.lastExercisesUpdateTime === undefined) {
                        data.lastExercisesUpdateTime = 0;
                    }

                    kotlinCallback(JSON.stringify(data));
                } else {
                    kotlinCallback(null);
                }
            }, () => {
                kotlinCallback(null);
            });

            exerciseListeners.set(listenerId, unsubscribe);
            return listenerId;
        },

        removeExerciseListener: (listenerId) => {
            const unsubscribe = exerciseListeners.get(listenerId);
            if (unsubscribe) {
                unsubscribe();
                exerciseListeners.delete(listenerId);
            }
        },

        listenToExerciseProgressByUserId: (userId, kotlinCallback) => {
            const listenerId = `exercise_${exerciseListenerCounter++}`;
            const ref = getExerciseDocRef(userId);

            const unsubscribe = onSnapshot(ref, (docSnap) => {
                if (docSnap.exists()) {
                    const data = docSnap.data();

                    if (data.lastExercisesUpdateTime instanceof Timestamp) {
                        data.lastExercisesUpdateTime = data.lastExercisesUpdateTime.toMillis();
                    }
                    if (data.lastExercisesUpdateTime === null || data.lastExercisesUpdateTime === undefined) {
                        data.lastExercisesUpdateTime = 0;
                    }

                    kotlinCallback(JSON.stringify(data));
                } else {
                    kotlinCallback(null);
                }
            }, () => {
                kotlinCallback(null);
            });

            exerciseListeners.set(listenerId, unsubscribe);
            return listenerId;
        },

        setExerciseProgress: async (jsonStr) => {
            try {
                const ref = getExerciseDocRef();
                const data = JSON.parse(jsonStr);

                if (data.lastExercisesUpdateTime === null || data.lastExercisesUpdateTime === undefined) {
                    data.lastExercisesUpdateTime = 0;
                }

                await setDoc(ref, data, { merge: true });
                return { success: true };
            } catch (e) {
                console.error("[Exercise] setExerciseProgress error:", e);
                return { success: false, error: e.message || "Unknown error" };
            }
        },

listenToEventsInRange: (fromMillis, toMillis, kotlinCallback) => {
    try {
        const userId = getUserId();
        const fromTs = Timestamp.fromMillis(Number(fromMillis));
        const toTs   = Timestamp.fromMillis(Number(toMillis));

        const q = query(
            collection(firestore, "events"),
            where("userId", "==", userId),
            where("timestamp", ">=", fromTs),
            where("timestamp", "<",  toTs)
        );

        const listenerId = `eventsListener_${Date.now()}`;
        const unsubscribe = onSnapshot(q, (snapshot) => {
            const events = [];
            snapshot.forEach(doc => {
                const data = doc.data();
                let ts = data.timestamp;
                if (ts && typeof ts.toMillis === "function") {
                    ts = ts.toMillis();
                }
                events.push({
                    id: doc.id,
                    eventType: data.eventType || "UNKNOWN",
                    meta: data.meta || {},
                    timeZone: data.timeZone || Intl.DateTimeFormat().resolvedOptions().timeZone,
                    timestamp: ts || null
                });
            });
            kotlinCallback(JSON.stringify(events));
        }, (error) => {
            console.error("[Firebase] listenToEventsInRange error:", error);
            kotlinCallback("[]");
        });

        teamListeners.set(listenerId, unsubscribe);
        return listenerId;
    } catch (error) {
        console.error("[Firebase] listenToEventsInRange setup error:", error);
        throw error;
    }
},

    removeEventsListener: (listenerId) => {
        const unsubscribe = projectListeners.get(listenerId);
        if (unsubscribe) {
            unsubscribe();
            projectListeners.delete(listenerId);
            }
        },
        
        // ===== Settings: one-time Settings hint (coachmark) =====
        getSettingsHintSeen: (kotlinCallback) => {
            const listenerId = `settingsHintListener_${settingsListenerCounter++}`;
            const docRef = doc(firestore, "users", getUserId(), "settings", "hints");

            const unsubscribe = onSnapshot(docRef, (docSnap) => {
                if (docSnap.exists()) {
                    const data = docSnap.data() || {};
                    const seen = !!data.settingsHintSeen;
                    kotlinCallback(seen ? "true" : "false");
                } else {
                    kotlinCallback(null);
                }
            }, (error) => {
                console.error("[Firebase] getSettingsHintSeen error:", error);
                kotlinCallback(null);
            });

            settingsListeners.set(listenerId, unsubscribe);
            return listenerId;
        },

        removeSettingsHintSeenListener: (listenerId) => {
            const unsubscribe = settingsListeners.get(listenerId);
            if (unsubscribe) {
                unsubscribe();
                settingsListeners.delete(listenerId);
            }
        },

        setSettingsHintSeen: async (seen) => {
            try {
                const docRef = doc(firestore, "users", getUserId(), "settings", "hints");
                await setDoc(docRef, { settingsHintSeen: !!seen }, { merge: true });
                return { success: true };
            } catch (error) {
                console.error("[Firebase] setSettingsHintSeen error:", error);
                return { success: false, error: error.message || "Unknown error" };
            }
        },

       seedTestEvents : async (count = 30) => {
            const userId = "sj1mL2O9eIcMVYrPz1vD4Qlp3N63";
            const eventsCollectionRef = collection(firestore, "events");
            const eventTypes = ["POMODORO_COMPLETED", "EXERCISE_HIGH_ENERGY_COMPLETED", "EXERCISE_LOW_ENERGY_COMPLETED", "TASK_COMPLETED"];
            let added = 0;

            for (let i = 0; i < count; i++) {
                const randomType = eventTypes[Math.floor(Math.random() * eventTypes.length)];

                const docData = {
                    userId: userId,
                    eventType: randomType,
                    meta: {},
                    timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
                    timestamp: randomTimestampSinceLastMonth()
                };

                try {
                    await addDoc(eventsCollectionRef, docData);
                    console.log(`✅ Добавлен event ${i + 1}`);
                    added++;
                } catch (err) {
                    console.error(`❌ Ошибка при добавлении ${i + 1}:`, err);
                }
            }

            console.log(`🎉 Готово. Успешно добавлено ${added} событий из ${count}.`);
        }
};

console.log("JS: Firebase bridge for Kotlin initialized.");