import { useEffect, useState, type FormEvent } from 'react'
import { toast } from 'sonner'
import { api, dateFr, type Livre, type PageLivres } from '@/api'
import { PaginationLivres } from '@/components/PaginationLivres'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'

type Props = { nom: string; onNom: (nom: string) => void }

export function Catalogue({ nom, onNom }: Props) {
  const [recherche, setRecherche] = useState('')
  const [page, setPage] = useState(0)
  const [resultat, setResultat] = useState<PageLivres | null>(null)
  const [erreur, setErreur] = useState<string | null>(null)
  const [choisi, setChoisi] = useState<Livre | null>(null)
  const [envoi, setEnvoi] = useState(false)
  const livres = resultat?.contenu ?? null

  const charger = (q: string, p: number) => {
    setErreur(null)
    api.livres(q, p).then(setResultat).catch((e: Error) => setErreur(e.message))
  }

  useEffect(() => {
    const t = setTimeout(() => charger(recherche, page), 250)
    return () => clearTimeout(t)
  }, [recherche, page])

  const changerPage = (p: number) => {
    setPage(p)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const emprunter = async (e: FormEvent) => {
    e.preventDefault()
    if (!choisi || !nom.trim()) return
    setEnvoi(true)
    try {
      const emprunt = await api.emprunter(choisi.id, nom.trim())
      toast.success(`« ${emprunt.titreLivre} » emprunté`, {
        description: `À rendre avant le ${dateFr(emprunt.dateRetourPrevue)}`,
      })
      setChoisi(null)
      charger(recherche, page)
    } catch (err) {
      toast.error((err as Error).message)
    } finally {
      setEnvoi(false)
    }
  }

  return (
    <section>
      <div className="mb-8 max-w-md">
        <Label htmlFor="recherche" className="sr-only">Rechercher un livre</Label>
        <Input
          id="recherche"
          type="search"
          placeholder="Titre ou auteur…"
          value={recherche}
          onChange={(e) => {
            setRecherche(e.target.value)
            setPage(0)
          }}
          className="h-11 bg-card text-base"
        />
      </div>

      {erreur && (
        <p role="alert" className="text-destructive">
          Le catalogue ne répond pas ({erreur}). Réessayez dans un instant.
        </p>
      )}

      {!livres && !erreur && (
        <ul className="etagere">
          {Array.from({ length: 12 }, (_, i) => (
            <li key={i}>
              <Skeleton className="aspect-[2/3] w-full rounded-sm" />
              <Skeleton className="mt-3 h-4 w-3/4" />
            </li>
          ))}
        </ul>
      )}

      {livres && livres.length === 0 && (
        <p className="text-muted-foreground">Aucun livre ne correspond à « {recherche} ».</p>
      )}

      {livres && livres.length > 0 && (
        <ul className="etagere">
          {livres.map((livre) => {
            const dispo = livre.exemplairesDisponibles > 0
            return (
              <li key={livre.id}>
                <button
                  type="button"
                  disabled={!dispo}
                  onClick={() => setChoisi(livre)}
                  className="couverture group"
                  aria-label={`Emprunter ${livre.titre}`}
                >
                  {livre.couvertureUrl ? (
                    <img src={livre.couvertureUrl} alt="" loading="lazy" />
                  ) : (
                    <span className="couverture-vide">{livre.titre}</span>
                  )}
                </button>
                <h3 className="mt-3 font-heading text-[0.95rem] leading-snug font-semibold">{livre.titre}</h3>
                <p className="text-sm text-muted-foreground">
                  {livre.auteurs ?? 'Auteur inconnu'}
                  {livre.annee ? `, ${livre.annee}` : ''}
                </p>
                <Badge variant={dispo ? 'secondary' : 'outline'} className="mt-2">
                  {dispo
                    ? `${livre.exemplairesDisponibles} sur ${livre.exemplairesTotal} en rayon`
                    : 'Tous empruntés'}
                </Badge>
              </li>
            )
          })}
        </ul>
      )}

      {resultat && (
        <>
          <PaginationLivres page={resultat.page} totalPages={resultat.totalPages} onPage={changerPage} />
          {resultat.totalElements > 0 && (
            <p className="mt-4 text-center text-sm text-muted-foreground">
              {resultat.totalElements} livre{resultat.totalElements > 1 ? 's' : ''}
            </p>
          )}
        </>
      )}

      <Dialog open={choisi !== null} onOpenChange={(ouvert) => !ouvert && setChoisi(null)}>
        <DialogContent>
          <form onSubmit={emprunter} className="grid gap-4">
            <DialogHeader>
              <DialogTitle className="font-heading text-lg">Emprunter « {choisi?.titre} »</DialogTitle>
              <DialogDescription>Le livre est à rendre sous 14 jours.</DialogDescription>
            </DialogHeader>
            <div className="grid gap-2">
              <Label htmlFor="nom">Votre nom</Label>
              <Input id="nom" value={nom} onChange={(e) => onNom(e.target.value)} required maxLength={80} autoFocus />
            </div>
            <DialogFooter>
              <Button type="submit" disabled={envoi || !nom.trim()}>
                {envoi ? 'Emprunt en cours…' : 'Emprunter'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </section>
  )
}
